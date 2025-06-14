import { Component, OnInit } from '@angular/core';
import { DeploymentDto, DeploymentStatusDto } from '../../models/deployment.model';
import { DeploymentService } from '../../services/deployment.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { DeployDialogComponent } from '../../dialogs/deploy-dialog/deploy-dialog.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-deployments-list',
  templateUrl: './deployments-list.component.html',
  styleUrls: ['./deployments-list.component.scss']
})
export class DeploymentsListComponent implements OnInit {
  deployments: DeploymentDto[] = [];
  deploymentStatuses: { [id: number]: DeploymentStatusDto } = {};
  displayedColumns: string[] = ['id', 'version', 'status', 'branch', 'createdAt', 'typeInfo', 'actions'];
  loadingActions: { [id: number]: { delete?: boolean; deploy?: boolean; kill?: boolean } } = {};
  isDeploying: boolean = false;

  filterForm: FormGroup;
  totalElements = 0;
  pageIndex = 0;
  pageSize = 5;

  highlightFirstRow = false;
  highlightedId: number | null = null;

  constructor(
    private deploymentService: DeploymentService,
    private dialog: MatDialog,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      applicationId: [''],
      createdDate: ['']
    });
  }

  ngOnInit(): void {
    this.loadDeployments();
    this.route.queryParams.subscribe(params => {
      const isNewDeploy = params['new-deploy'] === 'true';
      this.loadDeployments(isNewDeploy);
    });
  }

  loadDeployments(highlight: boolean = false): void {
    this.highlightFirstRow = highlight;

    const { applicationId, createdDate } = this.filterForm.value;
    const formattedDate = createdDate ? new Date(createdDate).toISOString().split('T')[0] : undefined;

    this.deploymentService.search(applicationId || undefined, formattedDate, this.pageIndex, this.pageSize)
      .subscribe({
        next: page => {
          this.deployments = page.content;
          this.totalElements = page.totalElements;
          this.loadStatuses();

          if (highlight && this.deployments.length > 0) {
            setTimeout(() => this.blinkRow(this.deployments[0].id!), 0);
          }
        },
        error: err => console.error('Failed to fetch deployments', err)
      });
  }

  blinkRow(id: number): void {
    this.highlightedId = id;
    const interval = setInterval(() => {
      this.highlightedId = this.highlightedId === id ? null : id;
    }, 500);

    setTimeout(() => {
      clearInterval(interval);
      this.highlightedId = null;
    }, 5000);
  }


  loadStatuses(): void {
    this.deployments.forEach(d => {
      this.deploymentService.getDeploymentStatus(d.applicationId).subscribe({
        next: status => this.deploymentStatuses[d.id!] = status,
        error: err => console.error(`Status error for deployment ${d.id}`, err)
      });
    });
  }

  onFilter(): void {
    this.pageIndex = 0;
    this.loadDeployments();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDeployments();
  }

  deleteDeployment(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Deployment',
        message: 'Are you sure you want to delete this deployment?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.setLoading(id, 'delete', true);
        this.deploymentService.deleteById(id).subscribe({
          next: () => {
            this.setLoading(id, 'delete', false);
            this.loadDeployments();
          },
          error: err => {
            console.error('Delete failed', err);
            this.setLoading(id, 'delete', false);
          }
        });
      }
    });
  }

  rollbackDeployment(targetDeployment: DeploymentDto): void {
    const runningDeployment = this.deployments.find(d =>
      d.applicationId === targetDeployment.applicationId &&
      d.status?.toUpperCase() === 'RUNNING'
    );

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Rollback',
        message: `Are you sure you want to rollback to version "${targetDeployment.version}"?`,
        confirmText: 'Rollback',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        const status = runningDeployment ? this.deploymentStatuses[runningDeployment.id!] : null;
        const hasRunningPid = !!status?.pid;

        if (hasRunningPid) {
          this.setLoading(runningDeployment!.id!, 'kill', true);
        }
        this.setLoading(targetDeployment.applicationId, 'deploy', true);
        this.isDeploying = true;

        const doDeploy = () => {
          this.deploymentService.deployApplication(
            targetDeployment.applicationId,
            targetDeployment.branch!,
            'ROLLBACK'
          ).subscribe({
            next: () => {
              if (hasRunningPid) this.setLoading(runningDeployment!.id!, 'kill', false);
              this.setLoading(targetDeployment.applicationId, 'deploy', false);
              this.isDeploying = false;
              window.location.href = '/deployments?new-deploy=true';
            },
            error: err => {
              console.error('Rollback failed', err);
              if (hasRunningPid) this.setLoading(runningDeployment!.id!, 'kill', false);
              this.setLoading(targetDeployment.applicationId, 'deploy', false);
              this.isDeploying = false;
            }
          });
        };

        if (hasRunningPid) {
          this.deploymentService.killProcess(status.pid!).subscribe({
            next: () => doDeploy(),
            error: err => {
              console.error('Kill failed', err);
              this.setLoading(runningDeployment!.id!, 'kill', false);
              this.setLoading(targetDeployment.applicationId, 'deploy', false);
              this.isDeploying = false;
            }
          });
        } else {
          doDeploy();
        }
      }
    });
  }

  killDeploymentProcess(deploymentId: number): void {
    const status = this.deploymentStatuses[deploymentId];
    if (!status?.pid) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Kill Process',
        message: `Kill process with PID ${status.pid}?`,
        confirmText: 'Kill',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.setLoading(deploymentId, 'kill', true);
        this.deploymentService.killProcess(status.pid!).subscribe({
          next: () => {
            this.setLoading(deploymentId, 'kill', false);
            this.loadDeployments();
          },
          error: err => {
            console.error('Kill failed', err);
            this.setLoading(deploymentId, 'kill', false);
          }
        });
      }
    });
  }

  setLoading(id: number, action: 'delete' | 'deploy' | 'kill', loading: boolean): void {
    if (!this.loadingActions[id]) this.loadingActions[id] = {};
    this.loadingActions[id][action] = loading;
  }

  downloadLogFile(logsPath: string): void {
    this.deploymentService.downloadLog(logsPath).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = logsPath.split('/').pop() || 'log.txt';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: err => {
        console.error('Download failed', err);
      }
    });
  }

}

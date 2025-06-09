import { Component, OnInit } from '@angular/core';
import { DeploymentDto, DeploymentStatusDto } from '../../models/deployment.model';
import { DeploymentService } from '../../services/deployment.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { DeployDialogComponent } from '../../dialogs/deploy-dialog/deploy-dialog.component';
import { FormBuilder, FormGroup } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-deployments-list',
  templateUrl: './deployments-list.component.html',
  styleUrls: ['./deployments-list.component.scss']
})
export class DeploymentsListComponent implements OnInit {
  deployments: DeploymentDto[] = [];
  deploymentStatuses: { [id: number]: DeploymentStatusDto } = {};
  displayedColumns: string[] = ['id', 'version', 'status', 'branch', 'createdAt', 'typeInfo'];
  loadingActions: { [id: number]: { delete?: boolean; deploy?: boolean; kill?: boolean } } = {};

  filterForm: FormGroup;
  totalElements = 0;
  pageIndex = 0;
  pageSize = 10;

  constructor(
    private deploymentService: DeploymentService,
    private dialog: MatDialog,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      applicationId: [''],
      createdDate: ['']
    });
  }

  ngOnInit(): void {
    this.loadDeployments();
  }

  loadDeployments(): void {
    const { applicationId, createdDate } = this.filterForm.value;

    const formattedDate = createdDate
      ? new Date(createdDate).toISOString().split('T')[0]
      : undefined;

    this.deploymentService.search(
      applicationId || undefined,
      formattedDate,
      this.pageIndex,
      this.pageSize
    ).subscribe({
      next: page => {
        this.deployments = page.content;
        this.totalElements = page.totalElements;
        this.loadStatuses();
      },
      error: err => console.error('Failed to fetch deployments', err)
    });
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

  openDeployDialog(applicationId: number): void {
    const dialogRef = this.dialog.open(DeployDialogComponent, {
      width: '400px',
      data: applicationId
    });

    dialogRef.afterClosed().subscribe(branch => {
      if (branch) {
        const confirmRef = this.dialog.open(ConfirmDialogComponent, {
          width: '400px',
          data: {
            title: 'Confirm Deployment',
            message: `Deploy application with branch "${branch}"?`,
            confirmText: 'Deploy',
            cancelText: 'Cancel'
          }
        });

        confirmRef.afterClosed().subscribe(confirmed => {
          if (confirmed) {
            this.setLoading(applicationId, 'deploy', true);
            this.deploymentService.deployApplication(applicationId, branch).subscribe({
              next: () => {
                this.setLoading(applicationId, 'deploy', false);
                this.loadDeployments();
              },
              error: err => {
                console.error('Deployment failed', err);
                this.setLoading(applicationId, 'deploy', false);
              }
            });
          }
        });
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
}

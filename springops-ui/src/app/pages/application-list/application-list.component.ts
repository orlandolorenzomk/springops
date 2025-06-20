import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ApplicationDto } from '../../models/application.model';
import { ApplicationService } from '../../services/application.service';
import { DeploymentService } from '../../services/deployment.service';
import { ApplicationFormComponent } from '../application-form/application-form.component';
import { ConfirmDialogComponent } from '../../dialogs/confirm-dialog/confirm-dialog.component';
import { DeploymentStatusDto } from '../../models/deployment.model';
import { DeployDialogComponent } from '../../dialogs/deploy-dialog/deploy-dialog.component';
import {ManageEnvDialogComponent} from "../../dialogs/manage-env-dialog/manage-env-dialog.component";
import { Router } from '@angular/router';
import {ViewLogsDialogComponent} from "../../dialogs/view-logs-dialog/view-logs-dialog.component";
import {StatsDialogComponent} from "../../dialogs/stats-dialog/stats-dialog.component";
import {
  ManageDependenciesDialogComponent
} from "../../dialogs/manage-dependencies-dialog/manage-dependencies-dialog.component";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {forkJoin} from "rxjs";

@Component({
  selector: 'app-application-list',
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.scss']
})
export class ApplicationListComponent implements OnInit {
  applications: ApplicationDto[] = [];
  applicationStatuses: { [id: number]: DeploymentStatusDto } = {};
  displayedColumns: string[] = ['id', 'name', 'description', 'port', 'createdAt', 'status', 'actions'];
  loadingActions: { [id: number]: { edit?: boolean; delete?: boolean; deploy?: boolean; kill?: boolean } } = {};
  globalLoading = false;

  constructor(
    private applicationService: ApplicationService,
    private deploymentService: DeploymentService,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.applicationService.findAll().subscribe(
      data => {
        this.applications = data;
        this.applications.forEach(app => {
          this.deploymentService.getDeploymentStatus(app.id).subscribe({
            next: status => this.applicationStatuses[app.id] = status,
            error: err => console.error()
          });
        });
      },
      error => console.error()
    );
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ApplicationFormComponent, {
      width: '1000px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadApplications();
    });
  }

  openEditDialog(application: ApplicationDto): void {
    this.setLoading(application.id, 'edit', true);
    const dialogRef = this.dialog.open(ApplicationFormComponent, {
      width: '1000px',
      data: { mode: 'edit', application }
    });

    dialogRef.afterClosed().subscribe(result => {
      this.setLoading(application.id, 'edit', false);
      if (result) this.loadApplications();
    });
  }

  deleteApplication(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Delete',
        message: 'Are you sure you want to delete this application?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.setLoading(id, 'delete', true);
        this.applicationService.deleteById(id).subscribe({
          next: () => {
            this.setLoading(id, 'delete', false);
            this.loadApplications();
          },
          error: err => {
            console.error();
            this.setLoading(id, 'delete', false);
          }
        });
      }
    });
  }

  openDeployDialog(appId: number, gitProjectHttpsUrl: string): void {
    const dialogRef = this.dialog.open(DeployDialogComponent, {
      width: '400px',
      data: {
        appId: appId,
        gitUrl: gitProjectHttpsUrl
      }
    });

    dialogRef.afterClosed().subscribe(branch => {
      if (branch) {
        const isRollback = branch.startsWith('deploy');
        const deployType = isRollback ? 'ROLLBACK' : 'CLASSIC';

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
            this.setLoading(appId, 'deploy', true);
            this.globalLoading = true;

            this.deploymentService.deployApplication(appId, branch, deployType).subscribe({
              next: res => {
                console.log('Deployment result:', res);
                this.setLoading(appId, 'deploy', false);
                this.globalLoading = false;
                this.loadApplications();
                this.router.navigate(['/deployments'], { queryParams: { 'new-deploy': true } });
              },
              error: err => {
                console.error();
                this.setLoading(appId, 'deploy', false);
                this.globalLoading = false;
              }
            });
          }
        });
      }
    });
  }

  killApplicationProcess(appId: number): void {
    const status = this.applicationStatuses[appId];
    if (!status?.pid) return;

    const confirmRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Kill',
        message: `Kill process with PID "${status.pid}"?`,
        confirmText: 'Kill',
        cancelText: 'Cancel'
      }
    });

    confirmRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.setLoading(appId, 'kill', true);
        this.deploymentService.killProcess(status.pid!).subscribe({
          next: res => {
            console.log('Process killed:', res);
            this.setLoading(appId, 'kill', false);
            this.loadApplications();
          },
          error: err => {
            console.error();
            this.setLoading(appId, 'kill', false);
          }
        });
      }
    });
  }

  setLoading(appId: number, action: 'edit' | 'delete' | 'deploy' | 'kill', isLoading: boolean): void {
    if (!this.loadingActions[appId]) {
      this.loadingActions[appId] = {};
    }
    this.loadingActions[appId][action] = isLoading;
  }

  getStatusLabel(appId: number): string {
    const status = this.applicationStatuses[appId];
    if (!status) return 'Loading...';
    return status.isRunning ? `Running (PID: ${status.pid}, Port: ${status.port})` : 'Stopped';
  }

  openManageEnvDialog(appId: number): void {
    this.dialog.open(ManageEnvDialogComponent, {
      width: '700px',
      data: appId
    });
  }

  isAnyActionLoading(appId: number): boolean {
    const actions = this.loadingActions[appId];
    return !!(actions?.edit || actions?.delete || actions?.kill || actions?.deploy);
  }

  openViewLogsDialog(appId: number): void {
    this.dialog.open(ViewLogsDialogComponent, {
      width: '600px',
      data: appId
    });
  }

  openStatsDialog(appId: number): void {
    const dialogRef = this.dialog.open(StatsDialogComponent, {
      width: '1600px',
      height: '1000px',
      data: {
        applicationId: appId,
        startTimestamp: '',  // user-entered ISO‑8601
        endTimestamp: ''     // user-entered ISO‑8601
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Optionally refresh if needed
        console.log('Stats dialog closed:', result);
      }
    });
  }

  openManageDependenciesDialog(applicationId: number): void {
    this.applicationService.findAll().subscribe(applications => {
      const filteredApps = applications.filter(app => app.id !== applicationId);

      this.applicationService.getDependencies(applicationId).subscribe(currentDependencies => {
        const dialogRef = this.dialog.open(ManageDependenciesDialogComponent, {
          width: '800px',
          height: '600px',
          data: {
            applications: filteredApps,
            currentDependencies: currentDependencies
          }
        });

        dialogRef.afterClosed().subscribe((selectedDependencies: number[] | undefined) => {
          if (selectedDependencies) {
            this.applicationService.updateDependencies(applicationId, selectedDependencies).subscribe({
              next: deps => {
                console.log('Dependencies updated:', deps);
              },
              error: err => {
                console.error();
              }
            });
          }
        });
      });
    });
  }
}

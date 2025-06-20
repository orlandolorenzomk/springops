import { Component, OnInit } from '@angular/core';
import { DashboardDto } from '../../models/dashboard.model';
import { DashboardService } from '../../services/dashboard.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import {ApplicationFormComponent} from "../application-form/application-form.component";
import {MatDialog} from "@angular/material/dialog";
import {UserFormComponent} from "../user-form/user-form.component";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  stats: DashboardDto | null = null;
  loading = false;

  statCards: { icon: string, label: string, valueKey: keyof DashboardDto }[] = [
    { icon: 'rocket_launch', label: 'Active Apps', valueKey: 'registeredApps' },
    { icon: 'check_circle', label: 'Running', valueKey: 'runningApps' },
    { icon: 'cloud', label: 'Environments', valueKey: 'environments' }
  ];


  constructor(
    private dashboardService: DashboardService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadDashboardStats();
  }

  private loadDashboardStats(): void {
    this.loading = true;
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load dashboard stats', 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  openNewAppDialog(): void {
    this.dialog.open(ApplicationFormComponent, {
      width: '1200px',
      autoFocus: false,
      disableClose: true,
      data: { mode: 'create' }
    });
  }

  getStatValue(key: keyof DashboardDto): number {
    return this.stats?.[key] ?? 0;
  }

  openNewUserDialog() {
    this.dialog.open(UserFormComponent, {
      width: '600px',
      autoFocus: false,
      disableClose: true,
      data: { user: null }
    })
  }
}

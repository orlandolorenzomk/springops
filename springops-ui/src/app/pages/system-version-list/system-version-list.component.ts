import { Component, OnInit } from '@angular/core';
import { SystemVersionService } from '../../services/system-version.service';
import { SystemVersionDto } from '../../models/system-version.model';
import { MatDialog } from '@angular/material/dialog';
import { SystemVersionFormComponent } from '../system-version-form/system-version-form.component';
import {ConfirmDialogComponent} from "../../dialogs/confirm-dialog/confirm-dialog.component";

@Component({
  selector: 'app-system-version-list',
  templateUrl: './system-version-list.component.html',
  styleUrls: ['./system-version-list.component.scss']
})
export class SystemVersionListComponent implements OnInit {
  systemVersions: SystemVersionDto[] = [];
  displayedColumns: string[] = ['type', 'name', 'version', 'path', 'createdAt', 'actions'];

  constructor(
    private systemVersionService: SystemVersionService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadSystemVersions();
  }

  loadSystemVersions(): void {
    this.systemVersionService.findAll().subscribe(
      data => this.systemVersions = data,
      error => console.error()
    );
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(SystemVersionFormComponent, {
      width: '600px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadSystemVersions();
      }
    });
  }

  openEditDialog(systemVersion: SystemVersionDto): void {
    const dialogRef = this.dialog.open(SystemVersionFormComponent, {
      width: '600px',
      data: { mode: 'edit', systemVersion }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadSystemVersions();
      }
    });
  }

  deleteSystemVersion(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '800px',
      data: {
        title: 'Confirm Delete',
        message: 'Are you sure you want to delete this system version?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.systemVersionService.delete(id).subscribe(
          () => this.loadSystemVersions(),
          error => console.error()
        );
      }
    });
  }
}

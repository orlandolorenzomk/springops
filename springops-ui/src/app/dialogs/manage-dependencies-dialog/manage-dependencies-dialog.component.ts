import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApplicationDto } from '../../models/application.model';

@Component({
  selector: 'app-manage-dependencies-dialog',
  templateUrl: './manage-dependencies-dialog.component.html',
  styleUrls: ['./manage-dependencies-dialog.component.scss']
})
export class ManageDependenciesDialogComponent implements OnInit {
  selectedIds: number[] = [];

  constructor(
    public dialogRef: MatDialogRef<ManageDependenciesDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: { applications: ApplicationDto[]; currentDependencies: number[] }
  ) {}

  ngOnInit(): void {
    this.selectedIds = [...this.data.currentDependencies];
  }

  confirm(): void {
    this.dialogRef.close(this.selectedIds);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

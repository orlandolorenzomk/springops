import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-deploy-dialog',
  template: `
    <h1 mat-dialog-title>Deploy Application</h1>
    <br>
    <div mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Branch Name</mat-label>
        <input matInput [(ngModel)]="branchName" [defaultValue]="defaultBranch"/>
      </mat-form-field>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [disabled]="!branchName" (click)="confirm()">Deploy</button>
    </div>
  `,
})
export class DeployDialogComponent {
  defaultBranch: string = 'main';
  branchName: string;

  constructor(
    private dialogRef: MatDialogRef<DeployDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public appId: number
  ) {
    this.branchName = this.defaultBranch;
  }

  confirm() {
    this.dialogRef.close(this.branchName);
  }
}

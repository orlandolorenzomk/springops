import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GitService } from '../../services/git.service';

@Component({
  selector: 'app-deploy-dialog',
  template: `
    <h1 mat-dialog-title>Deploy Application</h1>
    <mat-progress-bar *ngIf="isLoading" mode="indeterminate"></mat-progress-bar>
    <br>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Branch Name</mat-label>
        <input
          type="text"
          matInput
          [(ngModel)]="branchName"
          [matAutocomplete]="auto"
          placeholder="Enter or select a branch"
        />
        <mat-autocomplete #auto="matAutocomplete">
          <mat-option *ngFor="let branch of branches" [value]="branch">
            {{ branch }}
          </mat-option>
        </mat-autocomplete>
        <mat-hint *ngIf="!isLoading && branches.length === 0">
          No branches found.
        </mat-hint>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button
        mat-raised-button
        color="primary"
        [disabled]="!branchName.trim() || isLoading"
        (click)="confirm()"
      >
        Deploy
      </button>
    </mat-dialog-actions>
  `,
})
export class DeployDialogComponent implements OnInit {
  defaultBranch = 'main';
  branchName = this.defaultBranch;
  branches: string[] = [];
  isLoading = false;

  constructor(
    private dialogRef: MatDialogRef<DeployDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { appId: number; gitUrl: string },
    private gitService: GitService
  ) {}

  ngOnInit(): void {
    this.isLoading = true;
    this.gitService.getAvailableBranches(this.data.gitUrl).subscribe({
      next: (list: string[]) => {
        this.branches = list;
        this.branchName = list.includes(this.defaultBranch)
          ? this.defaultBranch
          : list[0] ?? '';
        this.isLoading = false;
      },
      error: () => {
        this.branches = [this.defaultBranch];
        this.branchName = this.defaultBranch;
        this.isLoading = false;
      },
    });
  }

  confirm(): void {
    this.dialogRef.close(this.branchName.trim());
  }
}

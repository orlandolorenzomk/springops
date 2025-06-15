import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-notes-dialog',
  templateUrl: './notes-dialog.component.html',
  styleUrls: ['./notes-dialog.component.scss']
})
export class NotesDialogComponent {
  notes: string;

  constructor(
    private dialogRef: MatDialogRef<NotesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { notes: string }
  ) {
    this.notes = data.notes;
  }

  save(): void {
    this.dialogRef.close(this.notes);
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}

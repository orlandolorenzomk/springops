import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SystemVersionService } from '../../services/system-version.service';
import { SystemVersionDto } from '../../models/system-version.model';

@Component({
  selector: 'app-system-version-form',
  templateUrl: './system-version-form.component.html',
  styleUrls: ['./system-version-form.component.scss']
})
export class SystemVersionFormComponent implements OnInit {
  form: FormGroup = this.fb.group({
    id: [null],
    type: ['', Validators.required],
    name: ['', Validators.required],
    version: ['', Validators.required],
    path: ['', Validators.required],
    createdAt: [null]
  });

  mode: 'create' | 'edit';
  versionTypes = ['JAVA', 'MAVEN'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<SystemVersionFormComponent>,
    private systemVersionService: SystemVersionService,
    @Inject(MAT_DIALOG_DATA) public data: { mode: 'create' | 'edit', systemVersion?: SystemVersionDto }
  ) {
    this.mode = data.mode;
  }

  ngOnInit(): void {
    if (this.mode === 'edit' && this.data.systemVersion) {
      this.form.patchValue(this.data.systemVersion);
    }
  }

  onSubmit(): void {
    if (this.form.valid) {
      const systemVersionDto = this.form.value as SystemVersionDto;

      if (this.mode === 'create') {
        this.systemVersionService.save(systemVersionDto).subscribe(
          () => this.dialogRef.close(true),
          error => console.error()
        );
      } else {
        this.systemVersionService.update(systemVersionDto.id, systemVersionDto).subscribe(
          () => this.dialogRef.close(true),
          error => console.error()
        );
      }
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

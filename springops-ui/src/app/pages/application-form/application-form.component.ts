import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ApplicationService } from "../../services/application.service";
import { ApplicationDto } from "../../models/application.model";
import { SystemVersionService } from '../../services/system-version.service';
import { SystemVersionDto } from '../../models/system-version.model';

@Component({
  selector: 'app-application-form',
  templateUrl: './application-form.component.html',
  styleUrls: ['./application-form.component.scss']
})
export class ApplicationFormComponent implements OnInit {
  form!: FormGroup;
  mode: 'create' | 'edit';
  javaVersions: SystemVersionDto[] = [];
  mavenVersions: SystemVersionDto[] = [];


  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ApplicationFormComponent>,
    private applicationService: ApplicationService,
    private systemVersionService: SystemVersionService,
    @Inject(MAT_DIALOG_DATA) public data: { mode: 'create' | 'edit', application?: ApplicationDto }
  ) {
    this.mode = data.mode;
  }

  ngOnInit(): void {
    this.initializeForm();
    this.loadSystemVersions();

    if (this.mode === 'edit' && this.data.application) {
      this.form.patchValue(this.data.application);
    }
  }

  initializeForm(): void {
    const memoryPattern = /^[0-9]+[mgMG]$/;

    this.form = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      port: [null, Validators.required],
      description: [''],
      gitProjectHttpsUrl: ['', Validators.required],
      gitProjectSshUrl: ['', Validators.required],
      javaSystemVersionId: [null, Validators.required],
      mvnSystemVersionId: [null, Validators.required],
      javaMinimumMemory: ['', Validators.pattern(memoryPattern)],
      javaMaximumMemory: ['', Validators.pattern(memoryPattern)]
    });
  }

  loadSystemVersions(): void {
    this.systemVersionService.findAll().subscribe(versions => {
      this.javaVersions = versions.filter(v => v.type === 'JAVA');
      this.mavenVersions = versions.filter(v => v.type === 'MAVEN');
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dto = this.form.value as ApplicationDto;

    // Provide defaults if empty
    dto.javaMinimumMemory = dto.javaMinimumMemory?.trim() || '512m';
    dto.javaMaximumMemory = dto.javaMaximumMemory?.trim() || '1024m';

    // Double-check format
    const memPattern = /^[0-9]+[mMgG]$/;
    if (!memPattern.test(dto.javaMinimumMemory) || !memPattern.test(dto.javaMaximumMemory)) {
      console.error();
      return;
    }

    if (this.mode === 'create') {
      this.applicationService.save(dto).subscribe(
        () => this.dialogRef.close(true),
        error => console.error()
      );
    } else {
      this.applicationService.update(dto.id, dto).subscribe(
        () => this.dialogRef.close(true),
        error => console.error()
      );
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

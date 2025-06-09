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
    this.form = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: [''],
      gitProjectHttpsUrl: ['', Validators.pattern(/^https:\/\/github\.com\/.+/)],
      javaSystemVersionId: [null, Validators.required],
      mvnSystemVersionId: [null, Validators.required]
    });
  }

  loadSystemVersions(): void {
    this.systemVersionService.findAll().subscribe(versions => {
      // Filter versions by type
      this.javaVersions = versions.filter(v => v.type === 'JAVA');
      this.mavenVersions = versions.filter(v => v.type === 'MAVEN');
    });
  }

  onSubmit(): void {
    if (this.form.valid) {
      const applicationDto = this.form.value as ApplicationDto;

      if (this.mode === 'create') {
        this.applicationService.save(applicationDto).subscribe(
          () => this.dialogRef.close(true),
          error => console.error('Error creating application', error)
        );
      } else {
        this.applicationService.update(applicationDto.id, applicationDto).subscribe(
          () => this.dialogRef.close(true),
          error => console.error('Error updating application', error)
        );
      }
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

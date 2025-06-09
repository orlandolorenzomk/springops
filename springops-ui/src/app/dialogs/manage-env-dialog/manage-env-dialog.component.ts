import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApplicationEnvService } from '../../services/application-env.service';
import { ApplicationEnv } from '../../models/application-env.model';

@Component({
  selector: 'app-manage-env-dialog',
  templateUrl: './manage-env-dialog.component.html',
  styleUrls: ['./manage-env-dialog.component.scss']
})
export class ManageEnvDialogComponent implements OnInit {
  envForm = this.fb.group({
    envs: this.fb.array<FormGroup>([])
  });

  constructor(
    private fb: FormBuilder,
    private envService: ApplicationEnvService,
    private dialogRef: MatDialogRef<ManageEnvDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public applicationId: number
  ) {}

  ngOnInit(): void {
    this.envService.getByApplicationId(this.applicationId).subscribe(envs => {
      const formGroups = envs.map(env =>
        this.fb.group({
          id: [env.id],
          name: [env.name, Validators.required],
          value: ['********'], // obfuscated in UI
          encrypted: [true],
          originalValue: [env.value]
        })
      );
      this.envs.clear();
      formGroups.forEach(g => this.envs.push(g));
    });
  }

  get envs(): FormArray<FormGroup> {
    return this.envForm.get('envs') as FormArray<FormGroup>;
  }

  addEnv(): void {
    this.envs.push(this.fb.group({
      id: [null],
      name: ['', Validators.required],
      value: [''],
      encrypted: [false],
      originalValue: ['']
    }));
  }

  removeEnv(index: number): void {
    this.envs.removeAt(index);
  }

  markEditable(envGroup: FormGroup): void {
    envGroup.patchValue({ encrypted: false, value: '' });
  }

  save(): void {
    const payload: ApplicationEnv[] = this.envs.controls.map(control => {
      const val = control.value;
      return {
        id: val.id,
        name: val.name,
        value: val.encrypted ? val.originalValue : val.value,
        applicationId: this.applicationId
      };
    });

    this.envService.save(this.applicationId, payload).subscribe(saved => {
      this.dialogRef.close(saved);
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}

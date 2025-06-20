import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { User, UserCreationDto } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  user: User | undefined;
  isEditMode: boolean = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private dialogRef: MatDialogRef<UserFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user?: User }
  ) {
    this.user = data.user;
    this.isEditMode = this.user != null;
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  initializeForm(): void {
    const passwordValidator = (control: any) => {
      const pattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
      return pattern.test(control.value) ? null : { invalidPassword: true };
    };

    this.form = this.fb.group({
      username: [
        this.user ? this.user.username : '',
        [Validators.required, Validators.pattern(/^[a-zA-Z0-9_-]{3,20}$/)]
      ],
      email: [
        this.user ? this.user.email : '',
        [Validators.required, Validators.email]
      ],
      password: [
        '',
        this.isEditMode ? [] : [Validators.required, Validators.minLength(8), passwordValidator]
      ],
      confirmPassword: [
        '',
        this.isEditMode ? [] : [Validators.required]
      ]
    });
  }

  onSubmit(): void {
    console.log('Submit triggered');

    if (!this.isEditMode && (this.form.invalid || this.loading)) return;

    if (!this.isEditMode || this.form.value.password) {
      if (this.form.value.password !== this.form.value.confirmPassword) {
        this.form.setErrors({ passwordMismatch: true });
        return;
      } else {
        this.form.setErrors(null);
      }
    }

    this.loading = true;

    const userData: Partial<UserCreationDto> = {
      username: this.form.value.username,
      email: this.form.value.email
    };

    if (!this.isEditMode || this.form.value.password) {
      userData.password = this.form.value.password;
    }

    const operation = this.isEditMode
      ? this.userService.updateUser(this.user!.id, userData)
      : this.userService.createUser(userData as UserCreationDto);

    operation.subscribe({
      next: () => {
        this.dialogRef.close(true);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

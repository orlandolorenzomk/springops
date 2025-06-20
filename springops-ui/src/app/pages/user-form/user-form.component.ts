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
    // Create a custom password validator function
    const passwordValidator = (control: any) => {
      const pattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
      return pattern.test(control.value) ? null : { invalidPassword: true };
    };

    this.form = this.fb.group({
      username: [this.user ? this.user.username : '', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_-]{3,20}$/)]],
      email: [this.user ? this.user.email : '', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), passwordValidator]],
      confirmPassword: ['', [Validators.required]]
    });
  }


  onSubmit(): void {
    if (this.form.invalid || this.loading) {
      return;
    }

    if( this.form.value.password !== this.form.value.confirmPassword) {
      this.form.setErrors({ passwordMismatch: true });
      return;
    }
    else {
      this.form.setErrors(null); // Clear any previous errors
    } 

    this.loading = true;
    const userData: UserCreationDto = {
      username: this.form.value.username,
      email: this.form.value.email,
      password: this.form.value.password 
    };
    console.log('User Data:', userData);
    const operation = this.user == null
      ? this.userService.createUser(userData)
      : this.userService.updateUser(this.user.id, userData);

    operation.subscribe({
      next: () => {
        this.dialogRef.close(true); // Pass true to indicate success
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.userService.handleErrors(error);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

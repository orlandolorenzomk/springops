import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  form: FormGroup;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    const email = this.form.get('email')?.value;
    const password = this.form.get('password')?.value;

    this.authService.login(email, password).subscribe({
      next: (response) => {
        const expiresAt = Date.now() + parseInt(response.expiration) * 60_000;
        localStorage.setItem('token', response.token);
        localStorage.setItem('expiresAt', expiresAt.toString());
        this.router.navigate(['/']);
      },
      error: () => {
        this.error = 'Invalid credentials';
      }
    });
  }
}

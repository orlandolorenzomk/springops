import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  constructor(
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      tap({
        next: (event) => {
          if (event instanceof HttpResponse) {
            const body = event.body as any;
            if (body?.error) {
              this.showError(body.error);
            }
          }
        },
        error: (error: HttpErrorResponse) => {
          const errBody = error.error || {};
          const msg = errBody.error || 'Unexpected server error';
          this.showError(msg);

          if (errBody.code === 'JWT_EXPIRED') {
            this.router.navigate(['/auth/login']);
          }
        }
      })
    );
  }

  private showError(message: string): void {
    this.snackBar.open(`❌ ${message}`, 'Close', {
      duration: 5000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}

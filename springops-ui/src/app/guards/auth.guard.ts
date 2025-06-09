import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAuthenticated = authService.isAuthenticated();

  console.log('AuthGuard: Authentication status:', isAuthenticated ? 'Authenticated' : 'Not Authenticated');

  return isAuthenticated ? true : router.createUrlTree(['/login']);
};

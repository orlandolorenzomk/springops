import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { SetupService } from '../services/setup.service';

@Injectable({
  providedIn: 'root'
})
export class SetupGuard implements CanActivate {
  constructor(private setupService: SetupService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.setupService.checkSetupStatus().pipe(
      map(status => {
        if (status.isSetupComplete) {
          localStorage.setItem('ipAddress', status.ipAddress);
          localStorage.setItem('serverName', status.serverName);
          localStorage.setItem('environment', status.environment);
          return true;
        }
        if (!status.isFirstAdminInitialized) {
          return this.router.createUrlTree(['/setup/initialize-admin']);
        }
        if (!status.isFilesRootInitialized) {
          return this.router.createUrlTree(['/setup/initialize-files-root']);
        }

        localStorage.setItem("ipAddress", status.ipAddress);
        localStorage.setItem("serverName", status.serverName);
        localStorage.setItem("environment", status.environment);

        return true;
      }),
      catchError(() => of(true))
    );
  }
}

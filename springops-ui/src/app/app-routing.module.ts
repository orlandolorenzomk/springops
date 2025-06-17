import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InitializeAdminComponent } from './pages/setup/initialize-admin/initialize-admin.component';
import { InitializeFilesRootComponent } from './pages/setup/initialize-files-root/initialize-files-root.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { NotfoundComponent } from './pages/notfound/notfound.component';
import { SetupGuard } from './guards/setup.guard';
import { LoginComponent } from './pages/auth/login/login.component';
import { authGuard } from "./guards/auth.guard";
import { ApplicationListComponent } from "./pages/application-list/application-list.component";
import { SystemVersionListComponent } from "./pages/system-version-list/system-version-list.component";
import { DeploymentsListComponent } from "./pages/deployments-list/deployments-list.component";
import { AuditComponent } from "./pages/audit/audit.component";
import { UserManagementComponent } from './pages/user-management/user-management.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard, SetupGuard]
  },
  {
    path: 'applications',
    component: ApplicationListComponent,
    canActivate: [authGuard, SetupGuard]
  },
  {
    path: 'system-versions',
    component: SystemVersionListComponent,
    canActivate: [authGuard, SetupGuard]
  },
  {
    path: 'deployments',
    component: DeploymentsListComponent,
    canActivate: [authGuard, SetupGuard]
  },
  {
    path: 'audits',
    component: AuditComponent,
    canActivate: [authGuard, SetupGuard]
  },
  {
    path: 'users',
    component: UserManagementComponent,
    // canActivate: [authGuard, SetupGuard] TODO: add guards
  },
  {
    path: 'setup',
    children: [
      {
        path: 'initialize-admin',
        component: InitializeAdminComponent
      },
      {
        path: 'initialize-files-root',
        component: InitializeFilesRootComponent
      },
      {
        path: '',
        redirectTo: 'initialize-admin',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        component: LoginComponent,
        canActivate: [SetupGuard]
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'not-found',
    component: NotfoundComponent
  },
  {
    path: '**',
    redirectTo: 'not-found'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollPositionRestoration: 'enabled',
    anchorScrolling: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

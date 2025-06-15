import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { InitializeAdminComponent } from './pages/setup/initialize-admin/initialize-admin.component';
import { InitializeFilesRootComponent } from './pages/setup/initialize-files-root/initialize-files-root.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { NotfoundComponent } from './pages/notfound/notfound.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { NavbarComponent } from './components/navbar/navbar.component';
import {MatToolbar} from "@angular/material/toolbar";
import {MatIcon, MatIconModule} from "@angular/material/icon";
import {MatAnchor, MatButton, MatIconButton} from "@angular/material/button";
import { LoginComponent } from './pages/auth/login/login.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatError, MatFormField, MatHint, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {NgOptimizedImage} from "@angular/common";
import {MatCard, MatCardActions, MatCardContent, MatCardTitle} from "@angular/material/card";
import { ApplicationListComponent } from './pages/application-list/application-list.component';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatRow, MatRowDef,
  MatTable
} from "@angular/material/table";
import { ApplicationFormComponent } from './pages/application-form/application-form.component';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";
import { ConfirmDialogComponent } from './dialogs/confirm-dialog/confirm-dialog.component';
import {AuthInterceptor} from "./interceptors/auth.interceptor";
import { SystemVersionListComponent } from './pages/system-version-list/system-version-list.component';
import { SystemVersionFormComponent } from './pages/system-version-form/system-version-form.component';
import {MatOption, MatSelect} from "@angular/material/select";
import {HttpErrorInterceptor} from "./interceptors/http-error";
import { DeployDialogComponent } from './dialogs/deploy-dialog/deploy-dialog.component';
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import { DeploymentsListComponent } from './pages/deployments-list/deployments-list.component';
import {MatPaginator} from "@angular/material/paginator";
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from "@angular/material/datepicker";
import {MatNativeDateModule} from "@angular/material/core";
import { MAT_DATE_LOCALE } from '@angular/material/core';
import {MatChip, MatChipListbox} from "@angular/material/chips";
import { ManageEnvDialogComponent } from './dialogs/manage-env-dialog/manage-env-dialog.component';
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import { ViewLogsDialogComponent } from './dialogs/view-logs-dialog/view-logs-dialog.component';
import { MatListModule } from '@angular/material/list';
import { FootbarComponent } from './components/footbar/footbar.component';
import {MatProgressBar} from "@angular/material/progress-bar";
import { NotesDialogComponent } from './dialogs/notes-dialog/notes-dialog.component';
import {MatAutocomplete, MatAutocompleteTrigger} from "@angular/material/autocomplete";

@NgModule({
  declarations: [
    AppComponent,
    InitializeAdminComponent,
    InitializeFilesRootComponent,
    DashboardComponent,
    NotfoundComponent,
    NavbarComponent,
    LoginComponent,
    ApplicationListComponent,
    ApplicationFormComponent,
    ConfirmDialogComponent,
    SystemVersionListComponent,
    SystemVersionFormComponent,
    DeployDialogComponent,
    DeploymentsListComponent,
    ManageEnvDialogComponent,
    ViewLogsDialogComponent,
    FootbarComponent,
    NotesDialogComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    MatToolbar,
    MatIcon,
    MatButton,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    NgOptimizedImage,
    MatCard,
    MatCardContent,
    MatCardTitle,
    MatCardActions,
    MatTable,
    MatColumnDef,
    MatHeaderCell,
    MatCell,
    MatHeaderCellDef,
    MatCellDef,
    MatIconButton,
    MatHeaderRow,
    MatRow,
    MatHeaderRowDef,
    MatRowDef,
    MatDialogContent,
    MatDialogActions,
    MatDialogTitle,
    MatDialogClose,
    MatError,
    MatHint,
    MatSelect,
    MatOption,
    FormsModule,
    MatProgressSpinner,
    MatPaginator,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatDatepicker,
    MatNativeDateModule,
    MatDatepickerModule,
    MatIconModule,
    MatChip,
    MatMenu,
    MatMenuTrigger,
    MatMenuItem,
    MatChipListbox,
    MatAnchor,
    MatListModule,
    MatProgressBar,
    MatAutocompleteTrigger,
    MatAutocomplete,
  ],
  providers: [
    provideClientHydration(),
    provideAnimationsAsync(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true
    },
    {
      provide: MAT_DATE_LOCALE,
      useValue: 'en-US'
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

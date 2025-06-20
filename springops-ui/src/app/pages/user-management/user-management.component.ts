import { Component, OnInit, ViewChild } from '@angular/core';
import { User } from '../../models/user.model';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { UserService } from '../../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserFormComponent } from '../user-form/user-form.component';
import { MatDialog } from '@angular/material/dialog';
import {ConfirmDialogComponent} from "../../dialogs/confirm-dialog/confirm-dialog.component";

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  readonly displayedColumns: string[] = ['username', 'email', 'createdAt', 'updatedAt', 'actions'];
  dataSource = new MatTableDataSource<User>([]);
  isLoading = false;

  @ViewChild(MatSort) sort!: MatSort;
  searchControl = new FormControl('');

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadUsers();

    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe((searchTerm) => {
      this.applyFilter(searchTerm || '');
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.filterPredicate = (user: User, filter: string) => {
      const search = filter.toLowerCase();
      return user.username.toLowerCase().includes(search) ||
        user.email.toLowerCase().includes(search);
    };
  }

  loadUsers(): void {
    if (this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.userService.findAll().subscribe({
      next: (users) => {
        this.dataSource.data = users;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  openUserEditDialog(user?: User): void {
    const dialogRef = this.dialog.open(UserFormComponent, {
      width: '1000px',
      data: { user: user || null }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  deleteUser(id: string): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Deletion',
        message: 'Are you sure you want to delete this user?',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.isLoading = true;
        this.userService.deleteUser(id).subscribe({
          next: () => {
            this.dataSource.data = this.dataSource.data.filter(user => user.id !== id);
            this.isLoading = false;
            this.snackBar.open('User deleted successfully', 'Dismiss', { duration: 3000 });
          },
          error: () => {
            this.isLoading = false;
          }
        });
      }
    });
  }


  applyFilter(value: string): void {
    this.dataSource.filter = value.trim().toLowerCase();
  }
}

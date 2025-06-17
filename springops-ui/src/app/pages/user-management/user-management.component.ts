import { Component, OnInit, ViewChild } from '@angular/core';
import { User } from '../../models/user.model';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { UserService } from '../../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss',

})
export class UserManagementComponent implements OnInit {
  readonly displayedColumns: string[] = ['username', 'email', 'createdAt', 'updatedAt', 'actions'];
  dataSource = new MatTableDataSource<User>([]);
  selectedId: string | null = null;
  isLoading = false;

  @ViewChild(MatSort) sort!: MatSort;
  searchControl = new FormControl('');
  isTESTING = true; // Set to true for testing purposes

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadUsers();

    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
    ).subscribe((searchParam) => {
      this.applyFilter(searchParam || '');
    });
  }

  loadUsers(): void {
    if (this.isLoading) {
      return; // Prevent multiple simultaneous requests
    }
    if (this.isTESTING) {
      this.dataSource.data = PLACEHOLDER_USERS;
      return;
    }

    this.isLoading = true;
    this.userService.findAll()
      .subscribe({
        next: (users) => {
          this.dataSource.data = users;
          this.isLoading = false;
        },
        error: (error: HttpErrorResponse) => {
          this.handleErrors(error);
        }
      });
  }

  openUserEditDialog(id?: string): void {
  }

  deleteUser(id: string): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.isLoading = true;
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.dataSource.data = this.dataSource.data.filter(user => user.id !== id);
          this.isLoading = false;
          this.snackBar.open('User deleted successfully', 'Dismiss', { duration: 3000 });
        },
        error: (error: HttpErrorResponse) => {
         this.handleErrors(error);
        }
      });
    }
  }

  handleErrors(error: HttpErrorResponse): void {
    console.error('An error occurred:', error);
    this.snackBar.open(error.status == 0 ? 'Connection to server lost' : error.error, 'Dismiss', { duration: 3000 });
    this.isLoading = false;
  }

  /**
   * Search for users in the dataSource based on a filter string
   * @param filterValue The string to filter users by
   */
  applyFilter(filterValue: string): void {
    filterValue = filterValue.trim().toLowerCase();
    this.dataSource.filter = filterValue;
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    // Enable case-insensitive filtering
    this.dataSource.filterPredicate = (data: User, filter: string) => {
      return data.username.toLowerCase().includes(filter) ||
        data.email.toLowerCase().includes(filter);
    };
  }
}
  const PLACEHOLDER_USERS: User[] = [
    {
      id: '1',
      username: 'john_doe',
      email: 'john@example.com',
      createdAt: new Date(2023, 0, 15),
      updatedAt: new Date(2023, 3, 20)
    },
    {
      id: '2',
      username: 'jane_smith',
      email: 'jane@example.com',
      createdAt: new Date(2023, 1, 10),
      updatedAt: new Date(2023, 3, 25)
    },
    {
      id: '3',
      username: 'bob_johnson',
      email: 'bob@example.com',
      createdAt: new Date(2023, 2, 5),
      updatedAt: new Date(2023, 5, 12)
    },
    {
      id: '4',
      username: 'alice_williams',
      email: 'alice@example.com',
      createdAt: new Date(2023, 3, 20),
      updatedAt: new Date(2023, 5, 15)
    },
    {
      id: '5',
      username: 'charlie_brown',
      email: 'charlie@example.com',
      createdAt: new Date(2023, 4, 8),
      updatedAt: new Date(2023, 5, 18)
    },
    {
      id: '6',
      username: 'emma_davis',
      email: 'emma@example.com',
      createdAt: new Date(2023, 0, 22),
      updatedAt: new Date(2023, 4, 10)
    },
    {
      id: '7',
      username: 'michael_taylor',
      email: 'michael@example.com',
      createdAt: new Date(2023, 1, 18),
      updatedAt: new Date(2023, 6, 1)
    },
    {
      id: '8',
      username: 'sophia_miller',
      email: 'sophia@example.com',
      createdAt: new Date(2023, 2, 27),
      updatedAt: new Date(2023, 4, 29)
    },
    {
      id: '9',
      username: 'daniel_wilson',
      email: 'daniel@example.com',
      createdAt: new Date(2023, 3, 12),
      updatedAt: new Date(2023, 6, 8)
    },
    {
      id: '10',
      username: 'olivia_moore',
      email: 'olivia@example.com',
      createdAt: new Date(2023, 4, 19),
      updatedAt: new Date(2023, 7, 2)
    },
    {
      id: '11',
      username: 'james_anderson',
      email: 'james@example.com',
      createdAt: new Date(2022, 11, 5),
      updatedAt: new Date(2023, 5, 25)
    },
    {
      id: '12',
      username: 'ava_jackson',
      email: 'ava@example.com',
      createdAt: new Date(2023, 0, 30),
      updatedAt: new Date(2023, 6, 12)
    },
    {
      id: '13',
      username: 'william_harris',
      email: 'william@example.com',
      createdAt: new Date(2023, 1, 24),
      updatedAt: new Date(2023, 4, 15)
    },
    {
      id: '14',
      username: 'emily_martin',
      email: 'emily@example.com',
      createdAt: new Date(2022, 9, 17),
      updatedAt: new Date(2023, 6, 20)
    },
    {
      id: '15',
      username: 'alexander_thompson',
      email: 'alex@example.com',
      createdAt: new Date(2023, 3, 8),
      updatedAt: new Date(2023, 7, 5)
    },
    {
      id: '16',
      username: 'mia_robinson',
      email: 'mia@example.com',
      createdAt: new Date(2022, 8, 12),
      updatedAt: new Date(2023, 2, 28)
    },
    {
      id: '17',
      username: 'ethan_clark',
      email: 'ethan@example.com',
      createdAt: new Date(2023, 2, 16),
      updatedAt: new Date(2023, 5, 7)
    },
    {
      id: '18',
      username: 'isabella_rodriguez',
      email: 'isabella@example.com',
      createdAt: new Date(2022, 7, 25),
      updatedAt: new Date(2023, 3, 14)
    },
    {
      id: '19',
      username: 'jacob_walker',
      email: 'jacob@example.com',
      createdAt: new Date(2023, 3, 30),
      updatedAt: new Date(2023, 6, 29)
    },
    {
      id: '20',
      username: 'madison_lewis',
      email: 'madison@example.com',
      createdAt: new Date(2022, 10, 9),
      updatedAt: new Date(2023, 4, 22)
    }
  ];
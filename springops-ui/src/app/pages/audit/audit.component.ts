import { Component, OnInit } from '@angular/core';
import { AuditDto, AuditService, AuditStatusDto } from '../../services/audit.service';
import { Page } from '../../models/deployment.model';
import { User } from '../../models/user.model'; // create if needed
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent implements OnInit {
  audits: AuditDto[] = [];
  availableStatuses: AuditStatusDto[] = [];
  availableUsers: User[] = [];

  displayedColumns: string[] = ['id', 'action', 'timestamp', 'user'];
  totalElements = 0;
  page = 0;
  size = 10;
  loading = false;

  filter = {
    userId: undefined as number | undefined,
    action: '',
    from: undefined as Date | undefined,
    to: undefined as Date | undefined,
  };

  constructor(
    private auditService: AuditService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadStatuses();
    this.loadUsers();
    this.fetchAudits();
  }

  loadStatuses(): void {
    this.auditService.getAvailableStatuses().subscribe((statuses) => {
      this.availableStatuses = statuses;
    });
  }

  loadUsers(): void {
    console.log('Loading users...');
    this.userService.findAll().subscribe((users) => {
      console.log('Users loaded:', users);
      this.availableUsers = users;
    });
  }

  fetchAudits(): void {
    this.loading = true;

    const filterDto: any = {
      userId: this.filter.userId,
      action: this.filter.action?.trim() || null,
      from: this.filter.from ? this.filter.from.toISOString() : null,
      to: this.filter.to ? this.filter.to.toISOString() : null
    };

    this.auditService.searchAuditsPost(filterDto, this.page, this.size).subscribe({
      next: (data: Page<AuditDto>) => {
        this.audits = data.content;
        this.totalElements = data.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.page = 0;
    this.fetchAudits();
  }

  onPageChange(event: any): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.fetchAudits();
  }
}

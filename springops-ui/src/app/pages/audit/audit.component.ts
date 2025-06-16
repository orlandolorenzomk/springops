import { Component, OnInit } from '@angular/core';
import {AuditDto, AuditService} from '../../services/audit.service';
import {Page} from "../../models/deployment.model";

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent implements OnInit {

  audits: AuditDto[] = [];
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
  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.fetchAudits();
  }

  fetchAudits(): void {
    this.loading = true;

    const from = this.filter.from ? this.filter.from.toISOString() : undefined;
    const to = this.filter.to ? this.filter.to.toISOString() : undefined;

    this.auditService.searchAudits(
      this.filter.userId,
      this.filter.action?.trim(),
      from,
      to,
      this.page,
      this.size
    ).subscribe((data: Page<AuditDto>) => {
      this.audits = data.content;
      this.totalElements = data.totalElements;
      this.loading = false;
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

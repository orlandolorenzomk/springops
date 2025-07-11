import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ApplicationLog } from '../../models/application-log.model';
import {LogsService} from "../../services/logs.service";
@Component({
  selector: 'app-view-logs-dialog',
  templateUrl: './view-logs-dialog.component.html',
  styleUrls: ['./view-logs-dialog.component.scss']
})
export class ViewLogsDialogComponent implements OnInit {
  logs: ApplicationLog[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA) public applicationId: number,
    private logsService: LogsService
  ) {}

  ngOnInit(): void {
    this.logsService.listLogs(this.applicationId).subscribe({
      next: logs => {
        this.logs = logs;
        this.loading = false;
      },
      error: err => {
        this.error = 'Failed to load logs.';
        console.error();
        this.loading = false;
      }
    });
  }

  downloadLog(filename: string): void {
  this.logsService.downloadLog(this.applicationId, filename).subscribe(blob => {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.style.display = 'none';
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
    window.URL.revokeObjectURL(url);
  });
}
}

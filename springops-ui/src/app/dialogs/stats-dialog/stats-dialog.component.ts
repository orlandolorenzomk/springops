import { Component, Inject, OnInit } from '@angular/core';
import { ApplicationStats } from '../../models/application-stats.model';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ApplicationStatsService } from '../../services/application-stats.service';

export interface StatsDialogData {
  applicationId: number;
  startTimestamp?: string;
  endTimestamp?: string;
}

@Component({
  selector: 'app-stats-dialog',
  templateUrl: './stats-dialog.component.html',
  styleUrls: ['./stats-dialog.component.scss']
})
export class StatsDialogComponent implements OnInit {
  start: Date;
  end: Date;
  stats: ApplicationStats[] = [];
  chartData: any[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: StatsDialogData,
    private statsService: ApplicationStatsService
  ) {
    const now = new Date();
    this.end = data.endTimestamp ? new Date(data.endTimestamp) : now;
    this.start = data.startTimestamp ? new Date(data.startTimestamp)
      : new Date(this.end.getTime() - 2 * 60 * 60 * 1000); // default: last 2 hours
  }

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    const startTs = this.start.toISOString();
    const endTs = this.end.toISOString();

    this.statsService
      .getStatsOverTimePeriod(this.data.applicationId, startTs, endTs)
      .subscribe(arr => {
        this.stats = arr;
        this.chartData = [
          {
            name: 'Memory (MB)',
            series: arr.map(s => ({
              name: s.timestamp,
              value: s.memoryMb
            }))
          },
          {
            name: 'CPU (%)',
            series: arr.map(s => ({
              name: s.timestamp,
              value: Math.round(s.cpuLoad * 100 * 100) / 100 // show percent with 2 decimals
            }))
          },
          {
            name: 'Available Memory (MB)',
            series: arr.map(s => ({
              name: s.timestamp,
              value: s.availMemMb
            }))
          }
        ];
      });
  }

  formatXAxis = (value: string): string => {
    const date = new Date(value);
    return `${date.getHours().toString().padStart(2, '0')}:` +
      `${date.getMinutes().toString().padStart(2, '0')}:` +
      `${date.getSeconds().toString().padStart(2, '0')}`;
  };

}

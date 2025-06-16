import { Component, OnInit } from '@angular/core';
import { OsInfoService } from '../../services/os-info.service';
import { OsInfo } from '../../models/os-info.model';

@Component({
  selector: 'app-os-info',
  templateUrl: './os-info.component.html',
  styleUrls: ['./os-info.component.scss']
})
export class OsInfoComponent implements OnInit {
  osInfo: OsInfo | null = null;
  formattedInfo: {key: string, value: string}[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(private osInfoService: OsInfoService) {}

  ngOnInit(): void {
    this.loadOsInfo();
  }

  loadOsInfo(): void {
    this.isLoading = true;
    this.error = null;

    this.osInfoService.getOsInfo().subscribe({
      next: (data) => {
        this.osInfo = data;
        this.formattedInfo = this.osInfoService.formatOsInfo(data);
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load system information. Please try again later.';
        this.isLoading = false;
        console.error('Error loading OS info:', err);
      }
    });
  }

  refresh(): void {
    this.loadOsInfo();
  }
}

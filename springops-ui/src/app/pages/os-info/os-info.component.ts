import { Component, OnInit } from '@angular/core';
import { OsInfoService } from '../../services/os-info.service';
import { OsInfo } from '../../models/os-info.model';

@Component({
  selector: 'app-os-info',
  templateUrl: './os-info.component.html',
  styleUrls: ['./os-info.component.scss']
})
export class OsInfoComponent implements OnInit {
  systemInfo: {
    hostname: string;
    memoryAvailable: string;
    memoryTotal: string;
    ipAddress: string;
    diskTotal: string;
    operatingSystem: string;
    diskFree: string
  } = {
    hostname: 'Loading...',
    operatingSystem: 'Loading...',
    memoryTotal: 'Loading...',
    memoryAvailable: 'Loading...',
    diskTotal: 'Loading...',
    diskFree: 'Loading...',
    ipAddress: 'Loading...'
  };

  isLoading = false;

  constructor(private osInfoService: OsInfoService) {}

  ngOnInit(): void {
    this.loadSystemInfo();
  }

  loadSystemInfo(): void {
    this.isLoading = true;
    this.osInfoService.getOsInfo().subscribe({
      next: (data: OsInfo) => {
        this.systemInfo = {
          hostname: data.hostname || 'Unknown',
          operatingSystem: data.operatingSystem || 'Unknown',
          memoryTotal: data.memoryTotal || 'Unavailable',
          memoryAvailable: data.memoryAvailable || 'Unavailable',
          diskTotal: data.diskTotal || 'Unavailable',
          diskFree: data.diskFree || 'Unavailable',
          ipAddress: data.ipAddress || 'Unknown'
        };
        this.isLoading = false;
      },
      error: () => {
        this.systemInfo = {
          hostname: 'Error',
          operatingSystem: 'Error',
          memoryTotal: 'Error',
          memoryAvailable: 'Error',
          diskTotal: 'Error',
          diskFree: 'Error',
          ipAddress: 'Error'
        };
        this.isLoading = false;
      }
    });
  }

  refresh(): void {
    this.loadSystemInfo();
  }
}

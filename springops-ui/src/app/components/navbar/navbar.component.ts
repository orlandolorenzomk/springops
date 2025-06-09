import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  ipAddress: string | null = null;
  serverName: string | null = null;
  environment: string | null = null;

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.ipAddress = localStorage.getItem('ipAddress');
      this.serverName = localStorage.getItem('serverName');
      this.environment = localStorage.getItem('environment');
    }
  }

  getEnvironmentColor(): string {
    switch(this.environment?.toLowerCase()) {
      case 'PRODUCTION': return 'warn';
      case 'UAT': return 'accent';
      default: return 'primary';
    }
  }
}

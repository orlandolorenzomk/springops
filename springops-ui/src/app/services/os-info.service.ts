import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OsInfo } from '../models/os-info.model';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class OsInfoService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  /**
   * Retrieves comprehensive operating system and hardware information from the server
   * @returns Observable containing OS information
   */
  getOsInfo(): Observable<OsInfo> {
    return this.http.get<OsInfo>(`${this.baseUrl}/os-info`);
  }

  /**
   * Formats OS information for display in the UI
   * @param osInfo Raw OS information from the server
   * @returns Formatted key-value pairs for display
   */
  formatOsInfo(osInfo: OsInfo): {key: string, value: string}[] {
    return Object.entries(osInfo).map(([key, value]) => ({
      key: this.formatKey(key),
      value: value
    }));
  }

  /**
   * Formats property keys for better display in the UI
   * @param key The original property key
   * @returns Human-readable formatted key
   */
  private formatKey(key: string): string {
    return key
      .replace(/([A-Z])/g, ' $1') // Add space before capital letters
      .replace(/^./, str => str.toUpperCase()) // Capitalize first letter
      .replace(/Cpu/g, 'CPU')
      .replace(/Os/g, 'OS')
      .replace(/Ip/g, 'IP')
      .replace(/Dns/g, 'DNS')
      .replace(/Mac/g, 'MAC')
      .trim();
  }
}

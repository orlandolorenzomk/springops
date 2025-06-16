import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardDto } from '../models/dashboard.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private baseUrl = environment.apiUrl + '/dashboard';

  constructor(private http: HttpClient) {}

  getStats(): Observable<DashboardDto> {
    return this.http.get<DashboardDto>(this.baseUrl);
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationLog } from '../models/application-log.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LogsService {
  private apiUrl = environment.apiUrl + '/logs';

  constructor(private http: HttpClient) {}

  listLogs(applicationId: number): Observable<ApplicationLog[]> {
    return this.http.get<ApplicationLog[]>(`${this.apiUrl}/list`, {
      params: { applicationId }
    });
  }

  downloadLog(applicationId: number, filename: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${applicationId}/download`, {
      params: { filename },
      responseType: 'blob'
    });
  }
}

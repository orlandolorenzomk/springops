import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from "../../environments/environment";
import {Page} from "../models/deployment.model"; // generic pagination model

export interface AuditDto {
  id: number;
  action: string;
  timestamp: string;
  details: { [key: string]: any };
  user: string;
}

export interface AuditStatusDto {
  status: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private baseUrl = environment.apiUrl + '/audits';

  constructor(private http: HttpClient) {}

  getAuditById(auditId: number): Observable<AuditDto> {
    return this.http.get<AuditDto>(`${this.baseUrl}/${auditId}`);
  }

  searchAuditsPost(
    filter: {
      userId?: string;
      action?: string;
      from?: string;
      to?: string;
    },
    page: number = 0,
    size: number = 10
  ): Observable<Page<AuditDto>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.post<Page<AuditDto>>(`${this.baseUrl}/search`, filter, { params });
  }


  getAvailableStatuses(): Observable<AuditStatusDto[]> {
    return this.http.get<AuditStatusDto[]>(`${this.baseUrl}/statuses`);
  }
}

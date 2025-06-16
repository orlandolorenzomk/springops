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


@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private baseUrl = environment.apiUrl + '/audits';

  constructor(private http: HttpClient) {}

  getAuditById(auditId: number): Observable<AuditDto> {
    return this.http.get<AuditDto>(`${this.baseUrl}/${auditId}`);
  }

  searchAudits(
    userId?: number,
    action?: string,
    from?: string,
    to?: string,
    page: number = 0,
    size: number = 10
  ): Observable<Page<AuditDto>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (userId != null) params = params.set('userId', userId);
    if (action) params = params.set('action', encodeURIComponent(action));
    console.log('Search Audits Params:', params.toString());
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return this.http.get<Page<AuditDto>>(this.baseUrl, { params });
  }
}

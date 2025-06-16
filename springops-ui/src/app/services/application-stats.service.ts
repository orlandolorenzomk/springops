import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {ApplicationStats} from "../models/application-stats.model";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ApplicationStatsService {

  private readonly apiUrl = environment.apiUrl + '/application-stats';

  constructor(private http: HttpClient) {}

  /**
   * Fetches stats over a specified time period for a given application.
   *
   * @param applicationId the ID of the application
   * @param startTimestamp ISO‑8601 start date/time (e.g. "2025-06-10T00:00:00Z")
   * @param endTimestamp ISO‑8601 end date/time (e.g. "2025-06-15T23:59:59Z")
   */
  getStatsOverTimePeriod(
    applicationId: number,
    startTimestamp: string,
    endTimestamp: string
  ): Observable<ApplicationStats[]> {
    const params = new HttpParams()
      .set('applicationId', applicationId.toString())
      .set('startTimestamp', startTimestamp)
      .set('endTimestamp', endTimestamp);

    return this.http.get<ApplicationStats[]>(this.apiUrl, { params });
  }
}

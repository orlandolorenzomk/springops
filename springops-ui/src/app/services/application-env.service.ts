import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {ApplicationEnv} from "../models/application-env.model";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ApplicationEnvService {
  private readonly baseUrl = environment.apiUrl + '/application-env';

  constructor(private http: HttpClient) {}

  getByApplicationId(applicationId: number): Observable<ApplicationEnv[]> {
    return this.http.get<ApplicationEnv[]>(`${this.baseUrl}/${applicationId}`);
  }

  save(applicationId: number, envs: ApplicationEnv[]): Observable<ApplicationEnv[]> {
    return this.http.post<ApplicationEnv[]>(`${this.baseUrl}?applicationId=${applicationId}`, envs);
  }

  delete(id: number): Observable<ApplicationEnv> {
    return this.http.delete<ApplicationEnv>(`${this.baseUrl}/${id}`);
  }
}

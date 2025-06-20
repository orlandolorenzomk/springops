import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationDto } from '../models/application.model';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private apiUrl = environment.apiUrl + '/applications';

  constructor(private http: HttpClient) { }

  findAll(): Observable<ApplicationDto[]> {
    return this.http.get<ApplicationDto[]>(this.apiUrl);
  }

  findById(id: number): Observable<ApplicationDto> {
    return this.http.get<ApplicationDto>(`${this.apiUrl}/${id}`);
  }

  save(applicationDto: ApplicationDto): Observable<ApplicationDto> {
    return this.http.post<ApplicationDto>(this.apiUrl, applicationDto);
  }

  update(id: number, applicationDto: ApplicationDto): Observable<ApplicationDto> {
    return this.http.put<ApplicationDto>(`${this.apiUrl}/${id}`, applicationDto);
  }

  deleteById(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateDependencies(applicationId: number, dependenciesId: number[]): Observable<number[]> {
    const params = dependenciesId.map(id => `dependenciesId=${id}`).join('&');
    return this.http.post<number[]>(`${this.apiUrl}/${applicationId}/dependencies?${params}`, {});
  }


  getDependencies(applicationId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${applicationId}/dependencies`);
  }
}

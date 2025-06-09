import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SystemVersionDto } from '../models/system-version.model';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SystemVersionService {
  private apiUrl = environment.apiUrl + '/system-versions';

  constructor(private http: HttpClient) { }

  findAll(): Observable<SystemVersionDto[]> {
    return this.http.get<SystemVersionDto[]>(this.apiUrl);
  }

  findById(id: number): Observable<SystemVersionDto> {
    return this.http.get<SystemVersionDto>(`${this.apiUrl}/${id}`);
  }

  save(systemVersionDto: SystemVersionDto): Observable<SystemVersionDto> {
    return this.http.post<SystemVersionDto>(this.apiUrl, systemVersionDto);
  }

  update(id: number, systemVersionDto: SystemVersionDto): Observable<SystemVersionDto> {
    return this.http.put<SystemVersionDto>(`${this.apiUrl}/${id}`, systemVersionDto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

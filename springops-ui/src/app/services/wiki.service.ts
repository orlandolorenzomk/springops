import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root',
})
export class WikiService {
  private baseUrl = environment.apiUrl + '/wiki';

  constructor(private http: HttpClient) {}

  getFileList(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/files`);
  }

  getFileContent(name: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/file`, {
      params: { name },
      responseType: 'text',
    });
  }
}

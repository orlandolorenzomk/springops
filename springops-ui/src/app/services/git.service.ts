import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class GitService {

  baseUrl = environment.apiUrl + '/git';

  constructor(private http: HttpClient) {}

  /**
   * Fetches available branches from the backend, excluding "deploy*" branches.
   * @param gitUrl The HTTP(S) URL of the Git repository.
   * @returns Observable resolving to an array of branch names.
   */
  getAvailableBranches(gitUrl: string): Observable<string[]> {
    const params = { gitUrl };
    return this.http.get<string[]>(this.baseUrl + '/available-branches', { params });
  }
}

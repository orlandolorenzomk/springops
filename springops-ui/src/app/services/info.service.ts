import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {InfoDto} from "../models/info.module";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class InfoService {
  private readonly apiUrl = environment.apiUrl + '/info';

  constructor(private http: HttpClient) {}

  getInfo(): Observable<InfoDto> {
    return this.http.get<InfoDto>(this.apiUrl);
  }
}

import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {SetupStatus} from "../models/setup-status.model";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SetupService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  checkSetupStatus(): Observable<SetupStatus> {
    return this.http.get<SetupStatus>(`${this.apiUrl}/setup/is-complete`);
  }
}

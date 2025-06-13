import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {DeploymentDto, DeploymentResultDto, DeploymentStatusDto, Page} from "../models/deployment.model";

@Injectable({
  providedIn: 'root'
})
export class DeploymentService {
  private apiUrlManager = environment.apiUrl + '/deployment-manager';
  private apiUrl = environment.apiUrl + '/deployments';

  constructor(private http: HttpClient) {}

  // Deployment Manager endpoints
  getDeploymentStatus(applicationId: number): Observable<DeploymentStatusDto> {
    return this.http.get<DeploymentStatusDto>(`${this.apiUrlManager}/status`, {
      params: { applicationId: applicationId.toString() }
    });
  }

  deployApplication(applicationId: number, branchName: string): Observable<DeploymentResultDto> {
    return this.http.post<DeploymentResultDto>(`${this.apiUrlManager}/deploy`, null, {
      params: {
        applicationId: applicationId.toString(),
        branchName: branchName
      }
    });
  }

  killProcess(pid: number): Observable<string> {
    return this.http.post<string>(`${this.apiUrlManager}/kill`, null, {
      params: { pid: pid.toString() }
    });
  }

  // Deployment CRUD + Search endpoints
  getById(id: number): Observable<DeploymentDto> {
    return this.http.get<DeploymentDto>(`${this.apiUrl}/${id}`);
  }

  getAll(): Observable<DeploymentDto[]> {
    return this.http.get<DeploymentDto[]>(`${this.apiUrl}`);
  }

  create(deployment: DeploymentDto): Observable<DeploymentDto> {
    return this.http.post<DeploymentDto>(this.apiUrl, deployment);
  }

  update(deployment: DeploymentDto): Observable<DeploymentDto> {
    return this.http.put<DeploymentDto>(this.apiUrl, deployment);
  }

  deleteById(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  search(applicationId?: number, createdDate?: string, page: number = 0, size: number = 10) {
    const params: any = {
      page,
      size,
    };

    if (applicationId) params.applicationId = applicationId;
    if (createdDate) params.createdDate = createdDate;

    return this.http.get<Page<DeploymentDto>>(`${this.apiUrl}/search`, { params });
  }

  downloadLog(filename: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/logs`, {
      params: { filename },
      responseType: 'blob'
    });
  }

}

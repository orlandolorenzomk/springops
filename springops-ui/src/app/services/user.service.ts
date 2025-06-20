import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { User, UserCreationDto } from '../models/user.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = environment.apiUrl + '/users';

  constructor(private httpClient: HttpClient, private snackBar: MatSnackBar) {}


   findAll(): Observable<User[]> {
    return this.httpClient.get<User[]>(this.API_URL);
  }

  findUserById(id: string): Observable<User> {
    return this.httpClient.get<User>(`${this.API_URL}/${id}`);
  }

  createUser(userDto: UserCreationDto): Observable<User> {
    return this.httpClient.post<User>(`${this.API_URL}/create`, userDto);
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.httpClient.patch<User>(`${this.API_URL}/${id}`, user);
  }

  deleteUser(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.API_URL}/${id}`);
  }
}

import { User } from '../models/user.model';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, catchError, Observable, tap, throwError} from 'rxjs';
import { Injectable } from '@angular/core';
import {Credential} from "../models/credential.model";
import {AuthResponse} from "../models/authResponse.model";

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  BASE_URL = 'http://localhost:8080/api/users/';

  private _userObject = new BehaviorSubject<User | null>(null);

  constructor(private http: HttpClient) {}

  login(credential: Credential): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(
        `${this.BASE_URL}login`,
        credential
      )
      .pipe(
        tap((loginData) => {
          if (loginData.accessToken && loginData.refreshToken) {
            localStorage.setItem('accessToken', loginData.accessToken);
            localStorage.setItem('refreshToken', loginData.refreshToken);
          }
          this._userObject.next(loginData.user);
        })
      );
  }

  refresh(): Observable<{ accessToken: string }> {
    const refreshToken = localStorage.getItem('refreshToken');
    return this.http
      .post<{ accessToken: string }>(`${this.BASE_URL}token/refresh`, {
        refreshToken,
      })
      .pipe(
        tap((tokenData) => {
          if (tokenData && tokenData.accessToken) {
            localStorage.setItem('accessToken', tokenData.accessToken);
          }
        })
      );
  }

  logout(): Observable<void> {
    const refreshToken = localStorage.getItem('refreshToken');
    this._userObject.next(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    return this.http.post<void>(`${this.BASE_URL}logout`, { });
  }

  signUp(credential: Credential): Observable<void>  {
    return this.http.post<void>(`${this.BASE_URL}register`, credential);
  }

  get userObject(): BehaviorSubject<User | null> {
    return this._userObject;
  }

  getMe(): Observable<{ user: User }> {
    return this.http.get<{ user: User }>(`${this.BASE_URL}getMe`).pipe(
      tap((user) => {
        this._userObject.next(user.user);
      }),
      catchError((err) => {
        console.log(err);
        return throwError(() => new Error('?'))
      })
    );
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse, AuthRequest, AuthResponse, RegisterRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(request: AuthRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        if (response.success) {
          localStorage.setItem('auth_token', response.data.accessToken);
          localStorage.setItem('auth_user', JSON.stringify(response.data));
          this.currentUserSubject.next(response.data);
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, request).pipe(
      tap(response => {
        if (response.success) {
          localStorage.setItem('auth_token', response.data.accessToken);
          localStorage.setItem('auth_user', JSON.stringify(response.data));
          this.currentUserSubject.next(response.data);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  private getStoredUser(): AuthResponse | null {
    const stored = localStorage.getItem('auth_user');
    return stored ? JSON.parse(stored) : null;
  }
}

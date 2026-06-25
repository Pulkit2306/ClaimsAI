import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly apiUrl = `${environment.apiUrl}/search`;

  constructor(private http: HttpClient) {}

  search(query: string, page = 0, size = 10): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get<ApiResponse<any>>(this.apiUrl, { params });
  }

  filterByType(claimType: string): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/filter/type/${claimType}`);
  }

  filterByStatus(status: string): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/filter/status/${status}`);
  }

  getAuditHistory(entityType: string, entityId: string): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.apiUrl}/audit/${entityType}/${entityId}`);
  }
}

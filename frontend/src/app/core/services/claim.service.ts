import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse } from '../models/user.model';
import { Claim, ClaimRequest, ClaimStats } from '../models/claim.model';

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private readonly apiUrl = `${environment.apiUrl}/claims`;

  constructor(private http: HttpClient) {}

  getAllClaims(page = 0, size = 10): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any>>(this.apiUrl, { params });
  }

  getClaimById(id: number): Observable<ApiResponse<Claim>> {
    return this.http.get<ApiResponse<Claim>>(`${this.apiUrl}/${id}`);
  }

  getClaimsByCustomer(customerId: number, page = 0, size = 10): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/customer/${customerId}`, { params });
  }

  getClaimStats(): Observable<ApiResponse<ClaimStats>> {
    return this.http.get<ApiResponse<ClaimStats>>(`${this.apiUrl}/stats`);
  }

  createClaim(request: ClaimRequest): Observable<ApiResponse<Claim>> {
    return this.http.post<ApiResponse<Claim>>(this.apiUrl, request);
  }

  updateClaimStatus(id: number, status: string): Observable<ApiResponse<Claim>> {
    return this.http.patch<ApiResponse<Claim>>(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  assignAdjuster(id: number, adjusterId: string): Observable<ApiResponse<Claim>> {
    return this.http.patch<ApiResponse<Claim>>(`${this.apiUrl}/${id}/assign?adjusterId=${adjusterId}`, {});
  }

  getSuspiciousClaims(threshold = 0.7): Observable<ApiResponse<Claim[]>> {
    const params = new HttpParams().set('threshold', threshold);
    return this.http.get<ApiResponse<Claim[]>>(`${this.apiUrl}/suspicious`, { params });
  }
}

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse } from '../models/user.model';
import { Policy, PolicyRequest, Customer } from '../models/policy.model';

@Injectable({ providedIn: 'root' })
export class PolicyService {
  private readonly apiUrl = `${environment.apiUrl}/policies`;
  private readonly customerUrl = `${environment.apiUrl}/customers`;

  constructor(private http: HttpClient) {}

  getAllPolicies(page = 0, size = 10): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any>>(this.apiUrl, { params });
  }

  getPolicyById(id: number): Observable<ApiResponse<Policy>> {
    return this.http.get<ApiResponse<Policy>>(`${this.apiUrl}/${id}`);
  }

  getPoliciesByCustomer(customerId: number): Observable<ApiResponse<Policy[]>> {
    return this.http.get<ApiResponse<Policy[]>>(`${this.apiUrl}/customer/${customerId}`);
  }

  createPolicy(request: PolicyRequest): Observable<ApiResponse<Policy>> {
    return this.http.post<ApiResponse<Policy>>(this.apiUrl, request);
  }

  updatePolicy(id: number, request: PolicyRequest): Observable<ApiResponse<Policy>> {
    return this.http.put<ApiResponse<Policy>>(`${this.apiUrl}/${id}`, request);
  }

  deletePolicy(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getAllCustomers(page = 0, size = 10): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<ApiResponse<any>>(this.customerUrl, { params });
  }

  createCustomer(customer: Customer): Observable<ApiResponse<Customer>> {
    return this.http.post<ApiResponse<Customer>>(this.customerUrl, customer);
  }
}

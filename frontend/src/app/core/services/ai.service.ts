import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResponse } from '../models/user.model';

export interface ChatRequest {
  message: string;
  sessionId?: string;
  claimId?: number;
}

export interface ChatResponse {
  response: string;
  sessionId: string;
  sourceDocs: string[];
}

export interface FraudAnalysisResult {
  claimId: number;
  fraudScore: number;
  riskLevel: string;
  summary: string;
  redFlags: string[];
  recommendation: string;
}

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly apiUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  chat(request: ChatRequest): Observable<ApiResponse<ChatResponse>> {
    return this.http.post<ApiResponse<ChatResponse>>(`${this.apiUrl}/chat`, request);
  }

  analyzeFraud(claimEvent: any): Observable<ApiResponse<FraudAnalysisResult>> {
    return this.http.post<ApiResponse<FraudAnalysisResult>>(`${this.apiUrl}/fraud/analyze`, claimEvent);
  }

  summarizeClaim(description: string, claimType: string): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(
      `${this.apiUrl}/claims/summarize?description=${encodeURIComponent(description)}&claimType=${claimType}`,
      {}
    );
  }

  ingestDocument(claimId: number, file: File): Observable<ApiResponse<string>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('claimId', claimId.toString());
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/documents/ingest`, formData);
  }
}

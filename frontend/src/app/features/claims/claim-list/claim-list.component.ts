import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ClaimService } from '@core/services/claim.service';
import { Claim } from '@core/models/claim.model';

@Component({
  selector: 'app-claim-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatPaginatorModule, MatButtonModule, MatIconModule],
  template: `
    <div class="container">
      <div class="page-header">
        <div>
          <h1>Claims</h1>
          <p class="page-subtitle">Manage and track insurance claims</p>
        </div>
        <a class="new-btn" routerLink="/claims/new">
          <mat-icon>add</mat-icon> New Claim
        </a>
      </div>

      <div class="claims-table-wrap glass-card">
        <table class="claims-table">
          <thead>
            <tr>
              <th>Claim #</th>
              <th>Type</th>
              <th>Status</th>
              <th>Amount</th>
              <th>Fraud Score</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let claim of claims; let i = index"
                [routerLink]="['/claims', claim.id]"
                class="claim-row"
                [style.animation-delay]="i * 0.05 + 's'">
              <td>
                <span class="claim-number">{{ claim.claimNumber }}</span>
              </td>
              <td>
                <span class="type-badge">{{ formatType(claim.claimType) }}</span>
              </td>
              <td>
                <span class="status-pill" [attr.data-status]="claim.status.toLowerCase()">
                  <span class="status-dot"></span>
                  {{ formatStatus(claim.status) }}
                </span>
              </td>
              <td class="amount">\${{ claim.estimatedAmount | number:'1.0-0' }}</td>
              <td>
                <div class="fraud-bar" *ngIf="claim.fraudScore">
                  <div class="fraud-fill" [style.width.%]="claim.fraudScore * 100"
                       [style.background]="getFraudGradient(claim.fraudScore)"></div>
                  <span class="fraud-value">{{ claim.fraudScore | number:'1.2-2' }}</span>
                </div>
                <span class="na" *ngIf="!claim.fraudScore">--</span>
              </td>
              <td class="date">{{ claim.createdAt | date:'MMM d, y' }}</td>
            </tr>
          </tbody>
        </table>

        <mat-paginator [length]="totalElements" [pageSize]="pageSize"
                       [pageSizeOptions]="[5, 10, 25]" (page)="onPage($event)">
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 32px; max-width: 1400px; margin: 0 auto; }
    .page-header {
      display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;
    }
    .page-header h1 { font-size: 28px; font-weight: 700; letter-spacing: -0.02em; }
    .page-subtitle { color: #64748b; font-size: 14px; margin-top: 4px; }

    .new-btn {
      display: flex; align-items: center; gap: 6px;
      padding: 10px 20px; border-radius: 12px;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      color: white; font-weight: 600; font-size: 13px;
      text-decoration: none; transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
    }
    .new-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 25px rgba(59, 130, 246, 0.4); }
    .new-btn mat-icon { font-size: 18px; width: 18px; height: 18px; }

    .claims-table-wrap { padding: 0; overflow: hidden; }
    .claims-table {
      width: 100%; border-collapse: collapse;
    }
    .claims-table th {
      text-align: left; padding: 14px 20px;
      font-size: 11px; font-weight: 600; text-transform: uppercase;
      letter-spacing: 0.06em; color: #64748b;
      border-bottom: 1px solid rgba(255,255,255,0.06);
    }
    .claim-row {
      cursor: pointer; transition: all 0.2s;
      animation: fadeInUp 0.4s ease-out both;
    }
    .claim-row:hover { background: rgba(59, 130, 246, 0.04); }
    .claim-row td {
      padding: 16px 20px; font-size: 13px;
      border-bottom: 1px solid rgba(255,255,255,0.04);
    }

    .claim-number { font-weight: 600; color: #3b82f6; font-family: 'JetBrains Mono', monospace; font-size: 12px; }
    .type-badge {
      padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 600;
      background: rgba(255,255,255,0.04); color: #94a3b8;
    }

    .status-pill {
      display: inline-flex; align-items: center; gap: 6px;
      padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600;
    }
    .status-dot {
      width: 6px; height: 6px; border-radius: 50%;
    }
    [data-status="submitted"] { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
    [data-status="submitted"] .status-dot { background: #f59e0b; }
    [data-status="under_review"] { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
    [data-status="under_review"] .status-dot { background: #3b82f6; }
    [data-status="adjuster_assigned"] { background: rgba(6, 182, 212, 0.1); color: #06b6d4; }
    [data-status="adjuster_assigned"] .status-dot { background: #06b6d4; }
    [data-status="investigation"] { background: rgba(249, 115, 22, 0.1); color: #f97316; }
    [data-status="investigation"] .status-dot { background: #f97316; }
    [data-status="approved"] { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    [data-status="approved"] .status-dot { background: #10b981; }
    [data-status="denied"] { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
    [data-status="denied"] .status-dot { background: #ef4444; }
    [data-status="settled"] { background: rgba(34, 197, 94, 0.1); color: #22c55e; }
    [data-status="settled"] .status-dot { background: #22c55e; }
    [data-status="flagged_fraud"] { background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); }
    [data-status="flagged_fraud"] .status-dot { background: #ef4444; animation: pulse 2s infinite; }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

    .amount { font-weight: 600; font-family: 'JetBrains Mono', monospace; font-size: 13px; }
    .date { color: #64748b; }
    .na { color: #334155; }

    .fraud-bar {
      display: flex; align-items: center; gap: 8px;
    }
    .fraud-fill {
      height: 4px; border-radius: 2px; min-width: 4px;
      transition: width 0.5s ease;
    }
    .fraud-value { font-size: 11px; font-weight: 600; color: #94a3b8; font-family: monospace; }

    @keyframes fadeInUp {
      from { opacity: 0; transform: translateY(12px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class ClaimListComponent implements OnInit {
  claims: Claim[] = [];
  totalElements = 0;
  pageSize = 10;

  constructor(private claimService: ClaimService) {}

  ngOnInit(): void {
    this.loadClaims(0, this.pageSize);
  }

  loadClaims(page: number, size: number): void {
    this.claimService.getAllClaims(page, size).subscribe({
      next: (response) => {
        this.claims = response.data.content;
        this.totalElements = response.data.totalElements;
      }
    });
  }

  onPage(event: PageEvent): void {
    this.loadClaims(event.pageIndex, event.pageSize);
  }

  formatType(type: string): string {
    return type.replace(/_/g, ' ');
  }

  formatStatus(status: string): string {
    return status.replace(/_/g, ' ');
  }

  getFraudGradient(score: number): string {
    if (score > 0.7) return 'linear-gradient(90deg, #f59e0b, #ef4444)';
    if (score > 0.4) return 'linear-gradient(90deg, #f59e0b, #f97316)';
    return 'linear-gradient(90deg, #10b981, #06b6d4)';
  }
}

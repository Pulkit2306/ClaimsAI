import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { PolicyService } from '@core/services/policy.service';
import { Policy } from '@core/models/policy.model';

@Component({
  selector: 'app-policy-list',
  standalone: true,
  imports: [CommonModule, MatPaginatorModule, MatIconModule],
  template: `
    <div class="container">
      <div class="page-header">
        <div>
          <h1>Policies</h1>
          <p class="page-subtitle">Active insurance policies</p>
        </div>
      </div>

      <div class="policies-grid">
        <div *ngFor="let policy of policies; let i = index"
             class="policy-card glass-card" [style.animation-delay]="i * 0.06 + 's'">
          <div class="policy-header">
            <div class="policy-type-icon" [style.background]="getTypeGradient(policy.policyType)">
              <mat-icon>{{ getTypeIcon(policy.policyType) }}</mat-icon>
            </div>
            <div>
              <div class="policy-number">{{ policy.policyNumber }}</div>
              <div class="policy-type">{{ policy.policyType }}</div>
            </div>
            <span class="active-badge" [class.inactive]="!policy.active">
              {{ policy.active ? 'Active' : 'Inactive' }}
            </span>
          </div>

          <p class="policy-desc">{{ policy.description }}</p>

          <div class="policy-details">
            <div class="detail">
              <span class="detail-label">Coverage</span>
              <span class="detail-value">\${{ policy.coverageAmount | number:'1.0-0' }}</span>
            </div>
            <div class="detail">
              <span class="detail-label">Premium</span>
              <span class="detail-value">\${{ policy.premiumAmount | number:'1.0-0' }}/yr</span>
            </div>
            <div class="detail">
              <span class="detail-label">Deductible</span>
              <span class="detail-value">\${{ policy.deductible | number:'1.0-0' }}</span>
            </div>
            <div class="detail">
              <span class="detail-label">Expires</span>
              <span class="detail-value">{{ policy.endDate | date:'MMM d, y' }}</span>
            </div>
          </div>
        </div>
      </div>

      <mat-paginator [length]="totalElements" [pageSize]="pageSize"
                     [pageSizeOptions]="[6, 12]" (page)="onPage($event)">
      </mat-paginator>
    </div>
  `,
  styles: [`
    .container { padding: 32px; max-width: 1400px; margin: 0 auto; }
    .page-header { margin-bottom: 24px; }
    .page-header h1 { font-size: 28px; font-weight: 700; letter-spacing: -0.02em; }
    .page-subtitle { color: #64748b; font-size: 14px; margin-top: 4px; }

    .policies-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px;
      margin-bottom: 24px; max-height: calc(100vh - 220px); overflow-y: auto;
      padding-right: 4px;
    }
    .policy-card {
      padding: 22px; animation: fadeInUp 0.4s ease-out both;
    }
    .policy-header { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
    .policy-type-icon {
      width: 40px; height: 40px; border-radius: 10px;
      display: flex; align-items: center; justify-content: center;
    }
    .policy-type-icon mat-icon { color: white; font-size: 20px; width: 20px; height: 20px; }
    .policy-number { font-size: 13px; font-weight: 600; color: #3b82f6; font-family: monospace; }
    .policy-type { font-size: 11px; color: #64748b; font-weight: 500; }

    .active-badge {
      margin-left: auto; padding: 4px 10px; border-radius: 8px; font-size: 11px; font-weight: 600;
      background: rgba(16,185,129,0.1); color: #10b981;
    }
    .active-badge.inactive { background: rgba(239,68,68,0.1); color: #ef4444; }

    .policy-desc { font-size: 13px; color: #94a3b8; line-height: 1.5; margin-bottom: 16px; }

    .policy-details { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding-top: 14px; border-top: 1px solid rgba(255,255,255,0.06); }
    .detail { display: flex; flex-direction: column; gap: 2px; }
    .detail-label { font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; color: #475569; }
    .detail-value { font-size: 14px; font-weight: 600; color: #e2e8f0; }

    @keyframes fadeInUp { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class PolicyListComponent implements OnInit {
  policies: Policy[] = [];
  totalElements = 0;
  pageSize = 12;

  constructor(private policyService: PolicyService) {}

  ngOnInit(): void { this.loadPolicies(0, this.pageSize); }

  loadPolicies(page: number, size: number): void {
    this.policyService.getAllPolicies(page, size).subscribe({
      next: (response) => {
        this.policies = response.data.content;
        this.totalElements = response.data.totalElements;
      }
    });
  }

  onPage(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.loadPolicies(event.pageIndex, event.pageSize);
  }

  getTypeIcon(type: string): string {
    const icons: Record<string, string> = { AUTO: 'directions_car', HOME: 'home', HEALTH: 'favorite', LIFE: 'volunteer_activism', COMMERCIAL: 'business', TRAVEL: 'flight' };
    return icons[type] || 'policy';
  }

  getTypeGradient(type: string): string {
    const gradients: Record<string, string> = {
      AUTO: 'linear-gradient(135deg, #3b82f6, #2563eb)',
      HOME: 'linear-gradient(135deg, #10b981, #059669)',
      HEALTH: 'linear-gradient(135deg, #ef4444, #dc2626)',
      LIFE: 'linear-gradient(135deg, #8b5cf6, #7c3aed)',
      COMMERCIAL: 'linear-gradient(135deg, #f59e0b, #d97706)',
      TRAVEL: 'linear-gradient(135deg, #06b6d4, #0891b2)',
    };
    return gradients[type] || 'linear-gradient(135deg, #64748b, #475569)';
  }
}

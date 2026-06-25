import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { ClaimService } from '@core/services/claim.service';
import { AiService } from '@core/services/ai.service';
import { Claim, ClaimStatus } from '@core/models/claim.model';

@Component({
  selector: 'app-claim-detail',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatSnackBarModule, FormsModule],
  template: `
    <div class="container" *ngIf="claim">
      <div class="page-header">
        <div>
          <div class="claim-id">{{ claim.claimNumber }}</div>
          <h1>{{ formatType(claim.claimType) }}</h1>
        </div>
        <span class="status-pill-lg" [attr.data-status]="claim.status.toLowerCase()">
          <span class="status-dot"></span>{{ formatStatus(claim.status) }}
        </span>
      </div>

      <div class="grid">
        <!-- Details -->
        <div class="detail-card glass-card">
          <div class="card-title"><mat-icon>info</mat-icon> Claim Details</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">Incident Date</span>
              <span class="value">{{ claim.incidentDate | date:'mediumDate' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Estimated Amount</span>
              <span class="value amount">\${{ claim.estimatedAmount | number:'1.0-0' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Approved Amount</span>
              <span class="value">{{ claim.approvedAmount ? ('$' + (claim.approvedAmount | number:'1.0-0')) : 'Pending' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Assigned Adjuster</span>
              <span class="value">{{ claim.assignedAdjusterId || 'Unassigned' }}</span>
            </div>
          </div>
          <div class="description">
            <span class="label">Description</span>
            <p>{{ claim.description }}</p>
          </div>
        </div>

        <!-- AI Analysis -->
        <div class="detail-card glass-card ai-card-detail">
          <div class="card-title"><mat-icon>auto_awesome</mat-icon> AI Analysis</div>

          <div class="fraud-section" *ngIf="claim.fraudScore">
            <div class="fraud-header">
              <span class="label">Fraud Risk Score</span>
              <span class="fraud-score" [style.color]="getFraudColor()">{{ claim.fraudScore | number:'1.2-2' }}</span>
            </div>
            <div class="fraud-bar-lg">
              <div class="fraud-fill-lg" [style.width.%]="claim.fraudScore * 100" [style.background]="getFraudGradient()"></div>
            </div>
          </div>

          <div class="ai-summary-section">
            <span class="label">AI Summary</span>
            <p *ngIf="claim.aiSummary">{{ claim.aiSummary }}</p>
            <p *ngIf="!claim.aiSummary" class="pending">Summary not yet generated</p>

            <button class="action-btn" (click)="generateSummary()" [disabled]="generatingSummary">
              <mat-icon>auto_awesome</mat-icon>
              {{ generatingSummary ? 'Generating...' : 'Generate AI Summary' }}
            </button>
          </div>
        </div>

        <!-- Status Update -->
        <div class="detail-card glass-card">
          <div class="card-title"><mat-icon>update</mat-icon> Update Status</div>
          <div class="status-grid">
            <button *ngFor="let s of statuses" class="status-option"
                    [class.current]="claim.status === s"
                    (click)="updateStatus(s)">
              {{ formatStatus(s) }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 32px; max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; }
    .claim-id { font-size: 13px; font-weight: 600; color: #3b82f6; margin-bottom: 4px; font-family: monospace; }
    .page-header h1 { font-size: 28px; font-weight: 700; letter-spacing: -0.02em; }

    .status-pill-lg {
      display: flex; align-items: center; gap: 8px;
      padding: 8px 16px; border-radius: 12px; font-size: 13px; font-weight: 600;
    }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; }
    [data-status="submitted"] { background: rgba(245,158,11,0.1); color: #f59e0b; }
    [data-status="submitted"] .status-dot { background: #f59e0b; }
    [data-status="under_review"] { background: rgba(59,130,246,0.1); color: #3b82f6; }
    [data-status="under_review"] .status-dot { background: #3b82f6; }
    [data-status="adjuster_assigned"] { background: rgba(6,182,212,0.1); color: #06b6d4; }
    [data-status="adjuster_assigned"] .status-dot { background: #06b6d4; }
    [data-status="investigation"] { background: rgba(249,115,22,0.1); color: #f97316; }
    [data-status="investigation"] .status-dot { background: #f97316; }
    [data-status="approved"] { background: rgba(16,185,129,0.1); color: #10b981; }
    [data-status="approved"] .status-dot { background: #10b981; }
    [data-status="denied"] { background: rgba(239,68,68,0.1); color: #ef4444; }
    [data-status="denied"] .status-dot { background: #ef4444; }
    [data-status="settled"] { background: rgba(34,197,94,0.1); color: #22c55e; }
    [data-status="settled"] .status-dot { background: #22c55e; }
    [data-status="flagged_fraud"] { background: rgba(239,68,68,0.15); color: #ef4444; }
    [data-status="flagged_fraud"] .status-dot { background: #ef4444; }

    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    .grid > :last-child { grid-column: 1 / -1; }
    .detail-card { padding: 24px; }
    .card-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; margin-bottom: 20px; }
    .card-title mat-icon { font-size: 20px; width: 20px; height: 20px; color: #3b82f6; }
    .ai-card-detail .card-title mat-icon { color: #8b5cf6; }

    .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
    .detail-item { display: flex; flex-direction: column; gap: 4px; }
    .label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; color: #64748b; }
    .value { font-size: 14px; font-weight: 500; color: #e2e8f0; }
    .amount { font-size: 20px; font-weight: 700; color: #f1f5f9; }

    .description { border-top: 1px solid rgba(255,255,255,0.06); padding-top: 16px; }
    .description p { font-size: 14px; color: #94a3b8; line-height: 1.6; margin-top: 8px; }

    .fraud-section { margin-bottom: 24px; }
    .fraud-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .fraud-score { font-size: 24px; font-weight: 800; }
    .fraud-bar-lg { height: 6px; border-radius: 3px; background: rgba(255,255,255,0.06); overflow: hidden; }
    .fraud-fill-lg { height: 100%; border-radius: 3px; transition: width 1s ease; }

    .ai-summary-section { border-top: 1px solid rgba(255,255,255,0.06); padding-top: 16px; }
    .ai-summary-section p { font-size: 14px; color: #94a3b8; line-height: 1.6; margin: 8px 0 16px; }
    .pending { color: #475569 !important; font-style: italic; }

    .action-btn {
      display: flex; align-items: center; gap: 6px;
      padding: 10px 18px; border-radius: 12px; border: none;
      background: linear-gradient(135deg, rgba(139,92,246,0.15), rgba(59,130,246,0.15));
      border: 1px solid rgba(139,92,246,0.2);
      color: #8b5cf6; font-size: 13px; font-weight: 600;
      cursor: pointer; transition: all 0.2s; font-family: inherit;
    }
    .action-btn:hover { background: linear-gradient(135deg, rgba(139,92,246,0.25), rgba(59,130,246,0.25)); }
    .action-btn:disabled { opacity: 0.5; }
    .action-btn mat-icon { font-size: 16px; width: 16px; height: 16px; }

    .status-grid { display: flex; flex-wrap: wrap; gap: 8px; }
    .status-option {
      padding: 8px 16px; border-radius: 10px; font-size: 12px; font-weight: 500;
      background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
      color: #94a3b8; cursor: pointer; transition: all 0.2s; font-family: inherit;
    }
    .status-option:hover { border-color: rgba(59,130,246,0.3); color: #e2e8f0; }
    .status-option.current { border-color: rgba(59,130,246,0.4); color: #3b82f6; background: rgba(59,130,246,0.08); }
  `]
})
export class ClaimDetailComponent implements OnInit {
  claim: Claim | null = null;
  statuses = Object.values(ClaimStatus);
  generatingSummary = false;

  constructor(
    private route: ActivatedRoute,
    private claimService: ClaimService,
    private aiService: AiService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.claimService.getClaimById(id).subscribe({
      next: (response) => this.claim = response.data
    });
  }

  formatType(type: string): string { return type.replace(/_/g, ' '); }
  formatStatus(status: string): string { return status.replace(/_/g, ' '); }

  updateStatus(status: string): void {
    if (!this.claim || this.claim.status === status) return;
    this.claimService.updateClaimStatus(this.claim.id, status).subscribe({
      next: (response) => {
        this.claim = response.data;
        this.snackBar.open('Status updated', 'Close', { duration: 2000 });
      }
    });
  }

  generateSummary(): void {
    if (!this.claim) return;
    this.generatingSummary = true;
    this.aiService.summarizeClaim(this.claim.description, this.claim.claimType).subscribe({
      next: (response) => {
        this.generatingSummary = false;
        if (this.claim) this.claim.aiSummary = response.data;
        this.snackBar.open('AI summary generated', 'Close', { duration: 2000 });
      },
      error: () => {
        this.generatingSummary = false;
        this.snackBar.open('Summary generation failed — check your API key', 'Close', { duration: 3000 });
      }
    });
  }

  getFraudColor(): string {
    if (!this.claim?.fraudScore) return '#64748b';
    if (this.claim.fraudScore > 0.7) return '#ef4444';
    if (this.claim.fraudScore > 0.4) return '#f59e0b';
    return '#10b981';
  }

  getFraudGradient(): string {
    if (!this.claim?.fraudScore) return '#334155';
    if (this.claim.fraudScore > 0.7) return 'linear-gradient(90deg, #f59e0b, #ef4444)';
    if (this.claim.fraudScore > 0.4) return 'linear-gradient(90deg, #f59e0b, #f97316)';
    return 'linear-gradient(90deg, #10b981, #06b6d4)';
  }
}

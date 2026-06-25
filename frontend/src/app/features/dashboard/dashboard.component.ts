import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { ClaimService } from '@core/services/claim.service';
import { AuthService } from '@core/services/auth.service';
import { ClaimStats } from '@core/models/claim.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  template: `
    <div class="dashboard">
      <!-- Hero Section -->
      <div class="hero">
        <div class="hero-content">
          <div class="greeting">
            <span class="wave">&#x1F44B;</span>
            <h1>Welcome back, <span class="name-gradient">{{ firstName }}</span></h1>
          </div>
          <p class="subtitle">Here's your insurance claims overview for today</p>
        </div>
        <div class="hero-orb orb-1"></div>
        <div class="hero-orb orb-2"></div>
        <div class="hero-orb orb-3"></div>
      </div>

      <!-- Stats Grid -->
      <div class="stats-grid">
        <div class="stat-card" *ngFor="let stat of statCards; let i = index"
             [style.animation-delay]="i * 0.1 + 's'">
          <div class="stat-icon-wrap" [style.background]="stat.bgGradient">
            <mat-icon>{{ stat.icon }}</mat-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
          <div class="stat-trend" [class.positive]="stat.trend > 0" [class.negative]="stat.trend < 0">
            <mat-icon>{{ stat.trend > 0 ? 'trending_up' : stat.trend < 0 ? 'trending_down' : 'trending_flat' }}</mat-icon>
            <span>{{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%</span>
          </div>
          <div class="stat-glow" [style.background]="stat.glowColor"></div>
        </div>
      </div>

      <!-- Action Cards -->
      <div class="section-header">
        <h2>Quick Actions</h2>
        <p>Navigate to key features</p>
      </div>

      <div class="actions-grid">
        <a class="action-card" routerLink="/claims/new">
          <div class="action-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb)">
            <mat-icon>add_circle_outline</mat-icon>
          </div>
          <div class="action-text">
            <h3>New Claim</h3>
            <p>Submit a new insurance claim</p>
          </div>
          <mat-icon class="action-arrow">arrow_forward</mat-icon>
        </a>

        <a class="action-card" routerLink="/claims">
          <div class="action-icon" style="background: linear-gradient(135deg, #06b6d4, #0891b2)">
            <mat-icon>assignment</mat-icon>
          </div>
          <div class="action-text">
            <h3>All Claims</h3>
            <p>View and manage all claims</p>
          </div>
          <mat-icon class="action-arrow">arrow_forward</mat-icon>
        </a>

        <a class="action-card" routerLink="/policies">
          <div class="action-icon" style="background: linear-gradient(135deg, #10b981, #059669)">
            <mat-icon>verified_user</mat-icon>
          </div>
          <div class="action-text">
            <h3>Policies</h3>
            <p>Browse active insurance policies</p>
          </div>
          <mat-icon class="action-arrow">arrow_forward</mat-icon>
        </a>

        <a class="action-card ai-card" routerLink="/ai-chat">
          <div class="action-icon" style="background: linear-gradient(135deg, #8b5cf6, #7c3aed)">
            <mat-icon>auto_awesome</mat-icon>
          </div>
          <div class="action-text">
            <h3>AI Assistant</h3>
            <p>Chat with AI about claims & policies</p>
          </div>
          <mat-icon class="action-arrow">arrow_forward</mat-icon>
          <div class="ai-badge">AI-POWERED</div>
        </a>

        <a class="action-card" routerLink="/search">
          <div class="action-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706)">
            <mat-icon>manage_search</mat-icon>
          </div>
          <div class="action-text">
            <h3>Smart Search</h3>
            <p>AI-powered semantic search</p>
          </div>
          <mat-icon class="action-arrow">arrow_forward</mat-icon>
        </a>
      </div>

      <!-- Tech Stack Banner -->
      <div class="tech-banner">
        <div class="tech-label">POWERED BY</div>
        <div class="tech-items">
          <span>Spring Boot</span><span class="dot"></span>
          <span>Angular</span><span class="dot"></span>
          <span>Kafka</span><span class="dot"></span>
          <span>Claude AI</span><span class="dot"></span>
          <span>Elasticsearch</span><span class="dot"></span>
          <span>PostgreSQL</span><span class="dot"></span>
          <span>pgvector RAG</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { padding: 0 32px 32px; max-width: 1400px; margin: 0 auto; }

    /* Hero */
    .hero {
      position: relative; padding: 48px 0 32px; overflow: hidden;
    }
    .hero-content { position: relative; z-index: 1; }
    .greeting { display: flex; align-items: center; gap: 12px; }
    .greeting h1 { font-size: 32px; font-weight: 700; letter-spacing: -0.02em; }
    .wave { font-size: 32px; animation: wave 2s ease-in-out infinite; display: inline-block; }
    @keyframes wave {
      0%, 100% { transform: rotate(0deg); }
      25% { transform: rotate(20deg); }
      75% { transform: rotate(-10deg); }
    }
    .name-gradient {
      background: linear-gradient(135deg, #3b82f6, #8b5cf6, #06b6d4);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .subtitle { color: #64748b; margin-top: 8px; font-size: 15px; }

    .hero-orb {
      position: absolute; border-radius: 50%; filter: blur(80px); pointer-events: none;
    }
    .orb-1 { width: 300px; height: 300px; background: rgba(59, 130, 246, 0.08); top: -100px; right: 10%; }
    .orb-2 { width: 200px; height: 200px; background: rgba(139, 92, 246, 0.06); top: 0; right: 30%; }
    .orb-3 { width: 250px; height: 250px; background: rgba(6, 182, 212, 0.05); top: -50px; right: 50%; }

    /* Stats */
    .stats-grid {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px; margin-bottom: 40px;
    }
    .stat-card {
      position: relative; overflow: hidden;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.06);
      border-radius: 16px; padding: 20px;
      display: flex; flex-wrap: wrap; align-items: flex-start; gap: 14px;
      transition: all 0.3s ease;
      animation: fadeInUp 0.5s ease-out both;
    }
    .stat-card:hover {
      border-color: rgba(59, 130, 246, 0.15);
      transform: translateY(-3px);
      box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
    }
    .stat-icon-wrap {
      width: 44px; height: 44px; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
    }
    .stat-icon-wrap mat-icon { color: white; font-size: 22px; width: 22px; height: 22px; }
    .stat-info { display: flex; flex-direction: column; }
    .stat-value { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; line-height: 1; }
    .stat-label { font-size: 12px; color: #64748b; margin-top: 4px; font-weight: 500; }
    .stat-trend {
      margin-left: auto; display: flex; align-items: center; gap: 2px;
      font-size: 12px; font-weight: 600; padding: 4px 8px; border-radius: 8px;
    }
    .stat-trend mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .stat-trend.positive { color: #10b981; background: rgba(16, 185, 129, 0.1); }
    .stat-trend.negative { color: #ef4444; background: rgba(239, 68, 68, 0.1); }
    .stat-glow {
      position: absolute; bottom: -30px; right: -30px;
      width: 100px; height: 100px; border-radius: 50%;
      filter: blur(40px); opacity: 0.15; pointer-events: none;
    }

    /* Section Header */
    .section-header { margin-bottom: 20px; }
    .section-header h2 { font-size: 20px; font-weight: 700; letter-spacing: -0.01em; }
    .section-header p { color: #64748b; font-size: 13px; margin-top: 4px; }

    /* Actions */
    .actions-grid {
      display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 14px; margin-bottom: 40px;
    }
    .action-card {
      position: relative; display: flex; align-items: center; gap: 14px;
      padding: 18px 20px; border-radius: 14px;
      background: rgba(255, 255, 255, 0.02);
      border: 1px solid rgba(255, 255, 255, 0.06);
      text-decoration: none; color: inherit;
      transition: all 0.3s ease; cursor: pointer;
    }
    .action-card:hover {
      background: rgba(255, 255, 255, 0.05);
      border-color: rgba(59, 130, 246, 0.2);
      transform: translateX(4px);
    }
    .action-card:hover .action-arrow { opacity: 1; transform: translateX(0); }
    .action-icon {
      width: 44px; height: 44px; border-radius: 12px; flex-shrink: 0;
      display: flex; align-items: center; justify-content: center;
    }
    .action-icon mat-icon { color: white; font-size: 22px; width: 22px; height: 22px; }
    .action-text h3 { font-size: 14px; font-weight: 600; color: #f1f5f9; }
    .action-text p { font-size: 12px; color: #64748b; margin-top: 2px; }
    .action-arrow {
      margin-left: auto; color: #64748b; font-size: 18px; width: 18px; height: 18px;
      opacity: 0; transform: translateX(-8px); transition: all 0.3s;
    }
    .ai-card { border-color: rgba(139, 92, 246, 0.15); }
    .ai-card:hover { border-color: rgba(139, 92, 246, 0.3); box-shadow: 0 0 30px rgba(139, 92, 246, 0.1); }
    .ai-badge {
      position: absolute; top: 8px; right: 12px;
      font-size: 9px; font-weight: 700; letter-spacing: 0.1em;
      padding: 2px 8px; border-radius: 6px;
      background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(59, 130, 246, 0.2));
      color: #8b5cf6; border: 1px solid rgba(139, 92, 246, 0.2);
    }

    /* Tech Banner */
    .tech-banner {
      display: flex; align-items: center; gap: 16px;
      padding: 14px 24px; border-radius: 12px;
      background: rgba(255, 255, 255, 0.02);
      border: 1px solid rgba(255, 255, 255, 0.04);
    }
    .tech-label {
      font-size: 10px; font-weight: 700; letter-spacing: 0.12em;
      color: #475569; white-space: nowrap;
    }
    .tech-items {
      display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
      font-size: 12px; color: #64748b; font-weight: 500;
    }
    .dot {
      width: 3px; height: 3px; border-radius: 50%;
      background: #334155; display: inline-block;
    }
  `]
})
export class DashboardComponent implements OnInit {
  firstName = '';
  stats: ClaimStats | null = null;
  statCards: { label: string; value: number; icon: string; bgGradient: string; glowColor: string; trend: number }[] = [];

  constructor(
    private claimService: ClaimService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.firstName = user?.fullName?.split(' ')[0] || 'User';

    this.claimService.getClaimStats().subscribe({
      next: (response) => {
        this.stats = response.data;
        this.statCards = [
          { label: 'Total Claims', value: this.stats.total, icon: 'assignment', bgGradient: 'linear-gradient(135deg, #3b82f6, #2563eb)', glowColor: '#3b82f6', trend: 12 },
          { label: 'Submitted', value: this.stats.submitted, icon: 'schedule_send', bgGradient: 'linear-gradient(135deg, #f59e0b, #d97706)', glowColor: '#f59e0b', trend: 8 },
          { label: 'Under Review', value: this.stats.underReview, icon: 'rate_review', bgGradient: 'linear-gradient(135deg, #06b6d4, #0891b2)', glowColor: '#06b6d4', trend: -3 },
          { label: 'Approved', value: this.stats.approved, icon: 'check_circle', bgGradient: 'linear-gradient(135deg, #10b981, #059669)', glowColor: '#10b981', trend: 15 },
          { label: 'Denied', value: this.stats.denied, icon: 'cancel', bgGradient: 'linear-gradient(135deg, #ef4444, #dc2626)', glowColor: '#ef4444', trend: -5 },
          { label: 'Fraud Flagged', value: this.stats.flaggedFraud, icon: 'gpp_bad', bgGradient: 'linear-gradient(135deg, #f97316, #ea580c)', glowColor: '#f97316', trend: 2 },
        ];
      }
    });
  }
}

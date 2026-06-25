import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { SearchService } from '@core/services/search.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatIconModule],
  template: `
    <div class="search-page">
      <div class="search-hero">
        <h1>Smart <span class="gradient-text">Search</span></h1>
        <p>AI-powered semantic search across all claims and policies</p>

        <div class="search-box">
          <mat-icon class="search-icon">search</mat-icon>
          <input [(ngModel)]="query" (keyup.enter)="search()"
                 placeholder="Search claims... e.g. 'water damage in Montreal'">
          <button class="search-btn" (click)="search()" [disabled]="!query.trim()">
            <mat-icon>arrow_forward</mat-icon>
          </button>
        </div>

        <div class="quick-filters">
          <button *ngFor="let f of filters" class="filter-chip" (click)="query = f; search()">{{ f }}</button>
        </div>
      </div>

      <div *ngIf="results.length > 0" class="results">
        <p class="results-count">{{ totalResults }} results found</p>

        <div *ngFor="let r of results; let i = index"
             class="result-card" [routerLink]="['/claims', r.claimId]"
             [style.animation-delay]="i * 0.08 + 's'">
          <div class="result-header">
            <span class="result-number">{{ r.claimNumber }}</span>
            <span class="result-type">{{ r.claimType }}</span>
            <span class="result-status" [attr.data-status]="r.status?.toLowerCase()">{{ r.status }}</span>
          </div>
          <p class="result-desc">{{ r.description }}</p>
          <div *ngIf="r.aiSummary" class="result-ai">
            <mat-icon>auto_awesome</mat-icon>
            <span>{{ r.aiSummary }}</span>
          </div>
        </div>
      </div>

      <div *ngIf="searched && results.length === 0" class="no-results">
        <mat-icon>search_off</mat-icon>
        <h3>No results found</h3>
        <p>Try different keywords or broaden your search</p>
      </div>
    </div>
  `,
  styles: [`
    .search-page { max-width: 860px; margin: 0 auto; padding: 48px 24px; }

    .search-hero { text-align: center; margin-bottom: 40px; }
    .search-hero h1 { font-size: 36px; font-weight: 800; letter-spacing: -0.03em; }
    .gradient-text {
      background: linear-gradient(135deg, #3b82f6, #8b5cf6, #06b6d4);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
    }
    .search-hero p { color: #64748b; margin-top: 8px; margin-bottom: 32px; }

    .search-box {
      display: flex; align-items: center; gap: 12px;
      padding: 6px 6px 6px 20px; border-radius: 16px;
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(255,255,255,0.08);
      transition: all 0.3s; max-width: 600px; margin: 0 auto;
    }
    .search-box:focus-within { border-color: rgba(59, 130, 246, 0.3); box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.08); }
    .search-icon { color: #475569; }
    .search-box input {
      flex: 1; background: none; border: none; outline: none;
      color: #f1f5f9; font-size: 15px; font-family: inherit; padding: 10px 0;
    }
    .search-box input::placeholder { color: #475569; }
    .search-btn {
      width: 40px; height: 40px; border-radius: 12px; border: none;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      color: white; cursor: pointer; display: flex; align-items: center; justify-content: center;
      transition: all 0.2s;
    }
    .search-btn:hover { box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3); }
    .search-btn:disabled { opacity: 0.4; }

    .quick-filters { display: flex; gap: 8px; justify-content: center; margin-top: 16px; flex-wrap: wrap; }
    .filter-chip {
      padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 500;
      background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
      color: #64748b; cursor: pointer; transition: all 0.2s; font-family: inherit;
    }
    .filter-chip:hover { background: rgba(59, 130, 246, 0.08); color: #e2e8f0; border-color: rgba(59, 130, 246, 0.2); }

    .results-count { color: #64748b; font-size: 13px; margin-bottom: 16px; }

    .result-card {
      padding: 20px; border-radius: 14px;
      background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.06);
      margin-bottom: 12px; cursor: pointer; transition: all 0.3s;
      text-decoration: none; color: inherit; display: block;
      animation: fadeInUp 0.4s ease-out both;
    }
    .result-card:hover { border-color: rgba(59, 130, 246, 0.2); transform: translateX(4px); }
    .result-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
    .result-number { font-weight: 700; color: #3b82f6; font-size: 13px; }
    .result-type { font-size: 11px; padding: 3px 8px; border-radius: 6px; background: rgba(255,255,255,0.04); color: #94a3b8; }
    .result-status { font-size: 11px; padding: 3px 8px; border-radius: 6px; font-weight: 600; }
    .result-desc { font-size: 13px; color: #94a3b8; line-height: 1.5; }
    .result-ai { display: flex; gap: 6px; margin-top: 10px; font-size: 12px; color: #8b5cf6; }
    .result-ai mat-icon { font-size: 14px; width: 14px; height: 14px; flex-shrink: 0; margin-top: 2px; }

    .no-results { text-align: center; padding: 60px 0; }
    .no-results mat-icon { font-size: 48px; width: 48px; height: 48px; color: #334155; }
    .no-results h3 { margin-top: 16px; color: #64748b; }
    .no-results p { color: #475569; font-size: 13px; }

    @keyframes fadeInUp { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
  `]
})
export class SearchComponent {
  query = '';
  results: any[] = [];
  totalResults = 0;
  searched = false;
  filters = ['auto collision', 'water damage', 'theft', 'fire', 'fraud flagged'];

  constructor(private searchService: SearchService) {}

  search(): void {
    if (!this.query.trim()) return;
    this.searched = true;
    this.searchService.search(this.query).subscribe({
      next: (response) => {
        this.results = response.data.content || [];
        this.totalResults = response.data.totalElements || 0;
      },
      error: () => {
        this.results = [];
        this.totalResults = 0;
      }
    });
  }
}

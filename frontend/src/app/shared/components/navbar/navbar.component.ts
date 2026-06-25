import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule, RouterLink, RouterLinkActive,
    MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule
  ],
  template: `
    <nav class="navbar" *ngIf="authService.isAuthenticated()">
      <div class="nav-left">
        <div class="logo" routerLink="/dashboard">
          <div class="logo-icon">
            <mat-icon>shield</mat-icon>
          </div>
          <span class="logo-text">Claims<span class="logo-ai">AI</span></span>
        </div>

        <div class="nav-links">
          <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">
            <mat-icon>space_dashboard</mat-icon>
            <span>Dashboard</span>
          </a>
          <a class="nav-link" routerLink="/claims" routerLinkActive="active">
            <mat-icon>assignment</mat-icon>
            <span>Claims</span>
          </a>
          <a class="nav-link" routerLink="/policies" routerLinkActive="active">
            <mat-icon>verified_user</mat-icon>
            <span>Policies</span>
          </a>
          <a class="nav-link" routerLink="/search" routerLinkActive="active">
            <mat-icon>manage_search</mat-icon>
            <span>Search</span>
          </a>
          <a class="nav-link ai-link" routerLink="/ai-chat" routerLinkActive="active">
            <mat-icon>auto_awesome</mat-icon>
            <span>AI Chat</span>
          </a>
        </div>
      </div>

      <div class="nav-right">
        <div class="user-pill" [matMenuTriggerFor]="menu">
          <div class="avatar">{{ getInitials() }}</div>
          <span class="user-name">{{ (authService.currentUser$ | async)?.fullName }}</span>
          <mat-icon class="chevron">expand_more</mat-icon>
        </div>
        <mat-menu #menu="matMenu" class="user-menu">
          <div class="menu-header">
            <div class="menu-avatar">{{ getInitials() }}</div>
            <div>
              <div class="menu-name">{{ (authService.currentUser$ | async)?.fullName }}</div>
              <div class="menu-role">{{ (authService.currentUser$ | async)?.role }}</div>
            </div>
          </div>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon> Sign Out
          </button>
        </mat-menu>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      position: sticky;
      top: 0;
      z-index: 1000;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 24px;
      height: 64px;
      background: rgba(10, 14, 26, 0.8);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    }

    .nav-left { display: flex; align-items: center; gap: 40px; }

    .logo {
      display: flex; align-items: center; gap: 10px; cursor: pointer;
      text-decoration: none;
    }
    .logo-icon {
      width: 36px; height: 36px; border-radius: 10px;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
    }
    .logo-icon mat-icon { color: white; font-size: 20px; width: 20px; height: 20px; }
    .logo-text { font-size: 20px; font-weight: 700; color: #f1f5f9; letter-spacing: -0.02em; }
    .logo-ai {
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .nav-links { display: flex; gap: 4px; }
    .nav-link {
      display: flex; align-items: center; gap: 6px;
      padding: 8px 14px; border-radius: 10px;
      color: #94a3b8; font-size: 13px; font-weight: 500;
      text-decoration: none; transition: all 0.2s;
    }
    .nav-link mat-icon { font-size: 18px; width: 18px; height: 18px; }
    .nav-link:hover { color: #f1f5f9; background: rgba(255, 255, 255, 0.05); }
    .nav-link.active {
      color: #3b82f6; background: rgba(59, 130, 246, 0.1);
    }
    .ai-link.active {
      color: #8b5cf6; background: rgba(139, 92, 246, 0.1);
    }

    .nav-right { display: flex; align-items: center; }
    .user-pill {
      display: flex; align-items: center; gap: 8px;
      padding: 4px 12px 4px 4px; border-radius: 24px;
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.06);
      cursor: pointer; transition: all 0.2s;
    }
    .user-pill:hover { background: rgba(255, 255, 255, 0.08); }
    .avatar {
      width: 32px; height: 32px; border-radius: 50%;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      font-size: 12px; font-weight: 700; color: white;
    }
    .user-name { font-size: 13px; font-weight: 500; color: #e2e8f0; }
    .chevron { font-size: 18px; width: 18px; height: 18px; color: #64748b; }

    .menu-header {
      display: flex; align-items: center; gap: 12px;
      padding: 12px 16px;
    }
    .menu-avatar {
      width: 40px; height: 40px; border-radius: 50%;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      font-size: 14px; font-weight: 700; color: white;
    }
    .menu-name { font-weight: 600; color: #f1f5f9; }
    .menu-role { font-size: 12px; color: #64748b; }
  `]
})
export class NavbarComponent {
  constructor(public authService: AuthService, private router: Router) {}

  getInitials(): string {
    const user = this.authService.getCurrentUser();
    if (!user?.fullName) return '?';
    return user.fullName.split(' ').map(n => n[0]).join('').toUpperCase();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}

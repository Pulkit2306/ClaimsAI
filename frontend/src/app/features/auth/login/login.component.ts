import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSnackBarModule
  ],
  template: `
    <div class="auth-page">
      <!-- Background Effects -->
      <div class="bg-grid"></div>
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>

      <!-- Left Panel -->
      <div class="left-panel">
        <div class="brand-section">
          <div class="brand-logo">
            <div class="logo-shape">
              <mat-icon>shield</mat-icon>
            </div>
            <h1>Claims<span>AI</span></h1>
          </div>
          <p class="tagline">AI-Powered Insurance Claims Management</p>

          <div class="features">
            <div class="feature" *ngFor="let f of features">
              <div class="feature-icon"><mat-icon>{{ f.icon }}</mat-icon></div>
              <div>
                <h4>{{ f.title }}</h4>
                <p>{{ f.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Panel -->
      <div class="right-panel">
        <div class="login-box">
          <h2>Welcome back</h2>
          <p class="login-subtitle">Sign in to your account</p>

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <div class="form-group">
              <label>Email</label>
              <div class="input-wrap">
                <mat-icon class="input-icon">email</mat-icon>
                <input type="email" formControlName="email" placeholder="name@company.com">
              </div>
            </div>

            <div class="form-group">
              <label>Password</label>
              <div class="input-wrap">
                <mat-icon class="input-icon">lock</mat-icon>
                <input [type]="hidePassword ? 'password' : 'text'" formControlName="password" placeholder="Enter your password">
                <mat-icon class="toggle-pass" (click)="hidePassword = !hidePassword">
                  {{ hidePassword ? 'visibility_off' : 'visibility' }}
                </mat-icon>
              </div>
            </div>

            <button class="submit-btn" type="submit" [disabled]="loginForm.invalid || loading">
              <span *ngIf="!loading">Sign In</span>
              <span *ngIf="loading" class="loading-dots">
                <span></span><span></span><span></span>
              </span>
            </button>
          </form>

          <div class="divider"><span>or</span></div>

          <p class="register-link">
            Don't have an account? <a routerLink="/auth/register">Create one</a>
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh; display: flex; position: relative; overflow: hidden;
      background: #050816;
    }

    .bg-grid {
      position: absolute; inset: 0; z-index: 0;
      background-image:
        linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px);
      background-size: 60px 60px;
    }
    .bg-orb { position: absolute; border-radius: 50%; filter: blur(120px); pointer-events: none; z-index: 0; }
    .orb-1 { width: 500px; height: 500px; background: rgba(59, 130, 246, 0.12); top: -10%; left: 10%; animation: float1 15s ease-in-out infinite; }
    .orb-2 { width: 400px; height: 400px; background: rgba(139, 92, 246, 0.1); bottom: -10%; right: 10%; animation: float2 18s ease-in-out infinite; }
    .orb-3 { width: 300px; height: 300px; background: rgba(6, 182, 212, 0.08); top: 50%; left: 50%; animation: float3 20s ease-in-out infinite; }

    @keyframes float1 { 0%, 100% { transform: translate(0, 0); } 50% { transform: translate(30px, -40px); } }
    @keyframes float2 { 0%, 100% { transform: translate(0, 0); } 50% { transform: translate(-40px, 30px); } }
    @keyframes float3 { 0%, 100% { transform: translate(0, 0); } 50% { transform: translate(20px, 20px); } }

    .left-panel {
      flex: 1; display: flex; align-items: center; justify-content: center;
      padding: 48px; position: relative; z-index: 1;
    }
    .brand-section { max-width: 440px; }
    .brand-logo { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; }
    .logo-shape {
      width: 52px; height: 52px; border-radius: 14px;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 8px 30px rgba(59, 130, 246, 0.35);
    }
    .logo-shape mat-icon { color: white; font-size: 28px; width: 28px; height: 28px; }
    .brand-logo h1 { font-size: 36px; font-weight: 800; color: #f1f5f9; letter-spacing: -0.03em; }
    .brand-logo h1 span {
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
    }
    .tagline { color: #64748b; font-size: 16px; margin-bottom: 48px; }

    .features { display: flex; flex-direction: column; gap: 24px; }
    .feature { display: flex; gap: 14px; align-items: flex-start; }
    .feature-icon {
      width: 40px; height: 40px; border-radius: 10px; flex-shrink: 0;
      background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.15);
      display: flex; align-items: center; justify-content: center;
    }
    .feature-icon mat-icon { color: #3b82f6; font-size: 20px; width: 20px; height: 20px; }
    .feature h4 { font-size: 14px; font-weight: 600; color: #e2e8f0; }
    .feature p { font-size: 13px; color: #64748b; margin-top: 2px; }

    .right-panel {
      flex: 1; display: flex; align-items: center; justify-content: center;
      padding: 48px; position: relative; z-index: 1;
    }
    .login-box {
      width: 100%; max-width: 400px; padding: 40px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.06);
      border-radius: 24px;
      backdrop-filter: blur(20px);
    }
    .login-box h2 { font-size: 26px; font-weight: 700; margin-bottom: 6px; color: #f1f5f9; }
    .login-subtitle { color: #64748b; margin-bottom: 32px; font-size: 14px; }

    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; font-size: 13px; font-weight: 500; color: #94a3b8; margin-bottom: 8px; }
    .input-wrap {
      display: flex; align-items: center; gap: 10px;
      padding: 0 14px; height: 48px; border-radius: 12px;
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      transition: all 0.2s;
    }
    .input-wrap:focus-within {
      border-color: rgba(59, 130, 246, 0.4);
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
    .input-icon { color: #475569; font-size: 18px; width: 18px; height: 18px; }
    .input-wrap input {
      flex: 1; background: none; border: none; outline: none;
      color: #f1f5f9; font-size: 14px; font-family: inherit;
    }
    .input-wrap input::placeholder { color: #475569; }
    .toggle-pass { color: #475569; font-size: 18px; width: 18px; height: 18px; cursor: pointer; }

    .submit-btn {
      width: 100%; height: 48px; border: none; border-radius: 12px;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      color: white; font-size: 15px; font-weight: 600;
      cursor: pointer; transition: all 0.3s; margin-top: 8px;
      box-shadow: 0 4px 20px rgba(59, 130, 246, 0.3);
    }
    .submit-btn:hover:not(:disabled) {
      box-shadow: 0 6px 30px rgba(59, 130, 246, 0.45);
      transform: translateY(-1px);
    }
    .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

    .loading-dots { display: flex; gap: 4px; justify-content: center; }
    .loading-dots span {
      width: 6px; height: 6px; border-radius: 50%; background: white;
      animation: dotPulse 1.2s ease-in-out infinite;
    }
    .loading-dots span:nth-child(2) { animation-delay: 0.2s; }
    .loading-dots span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes dotPulse { 0%, 100% { opacity: 0.3; } 50% { opacity: 1; } }

    .divider {
      display: flex; align-items: center; gap: 16px; margin: 24px 0;
      color: #334155; font-size: 12px;
    }
    .divider::before, .divider::after {
      content: ''; flex: 1; height: 1px; background: rgba(255,255,255,0.06);
    }

    .register-link { text-align: center; font-size: 13px; color: #64748b; }
    .register-link a { color: #3b82f6; font-weight: 600; text-decoration: none; }
    .register-link a:hover { color: #60a5fa; }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = true;
  loading = false;
  features = [
    { icon: 'auto_awesome', title: 'AI-Powered Analysis', desc: 'Fraud detection & claim assessment with Claude AI' },
    { icon: 'bolt', title: 'Real-time Processing', desc: 'Event-driven architecture with Apache Kafka' },
    { icon: 'search', title: 'Semantic Search', desc: 'Coveo-style intelligent search with Elasticsearch' },
    { icon: 'security', title: 'Enterprise Security', desc: 'OAuth2 + JWT with role-based access control' },
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    this.loading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message || 'Login failed', 'Close', { duration: 3000 });
      }
    });
  }
}

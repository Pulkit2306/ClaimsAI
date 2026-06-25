import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatIconModule, MatSnackBarModule],
  template: `
    <div class="auth-page">
      <div class="bg-grid"></div>
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>

      <div class="register-container">
        <div class="register-box">
          <div class="brand-mini">
            <div class="logo-shape"><mat-icon>shield</mat-icon></div>
            <span>Claims<span class="ai">AI</span></span>
          </div>

          <h2>Create your account</h2>
          <p class="sub">Get started with AI-powered claims management</p>

          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            <div class="row">
              <div class="form-group">
                <label>First Name</label>
                <div class="input-wrap">
                  <input formControlName="firstName" placeholder="First name">
                </div>
              </div>
              <div class="form-group">
                <label>Last Name</label>
                <div class="input-wrap">
                  <input formControlName="lastName" placeholder="Last name">
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>Email</label>
              <div class="input-wrap">
                <mat-icon class="input-icon">email</mat-icon>
                <input formControlName="email" type="email" placeholder="name@company.com">
              </div>
            </div>

            <div class="form-group">
              <label>Password</label>
              <div class="input-wrap">
                <mat-icon class="input-icon">lock</mat-icon>
                <input formControlName="password" type="password" placeholder="Min 8 characters">
              </div>
            </div>

            <div class="form-group">
              <label>Role</label>
              <div class="role-options">
                <label class="role-option" *ngFor="let r of roles"
                       [class.selected]="registerForm.get('role')?.value === r.value">
                  <input type="radio" formControlName="role" [value]="r.value">
                  <mat-icon>{{ r.icon }}</mat-icon>
                  <span>{{ r.label }}</span>
                </label>
              </div>
            </div>

            <button class="submit-btn" type="submit" [disabled]="registerForm.invalid || loading">
              {{ loading ? 'Creating account...' : 'Create Account' }}
            </button>
          </form>

          <p class="login-link">Already have an account? <a routerLink="/auth/login">Sign in</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #050816; position: relative; overflow: hidden; }
    .bg-grid { position: absolute; inset: 0; background-image: linear-gradient(rgba(255,255,255,0.02) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.02) 1px, transparent 1px); background-size: 60px 60px; }
    .bg-orb { position: absolute; border-radius: 50%; filter: blur(120px); }
    .orb-1 { width: 400px; height: 400px; background: rgba(59, 130, 246, 0.1); top: -10%; right: 20%; }
    .orb-2 { width: 300px; height: 300px; background: rgba(139, 92, 246, 0.08); bottom: -5%; left: 15%; }

    .register-container { position: relative; z-index: 1; width: 100%; max-width: 520px; padding: 24px; }
    .register-box {
      padding: 40px; border-radius: 24px;
      background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
      backdrop-filter: blur(20px);
    }

    .brand-mini { display: flex; align-items: center; gap: 10px; margin-bottom: 28px; }
    .logo-shape { width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg, #3b82f6, #8b5cf6); display: flex; align-items: center; justify-content: center; }
    .logo-shape mat-icon { color: white; font-size: 18px; width: 18px; height: 18px; }
    .brand-mini span { font-size: 20px; font-weight: 700; color: #f1f5f9; }
    .ai { background: linear-gradient(135deg, #3b82f6, #8b5cf6); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }

    h2 { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
    .sub { color: #64748b; font-size: 14px; margin-bottom: 28px; }

    .row { display: flex; gap: 12px; }
    .row .form-group { flex: 1; }
    .form-group { margin-bottom: 18px; }
    .form-group label { display: block; font-size: 13px; font-weight: 500; color: #94a3b8; margin-bottom: 6px; }
    .input-wrap {
      display: flex; align-items: center; gap: 8px; padding: 0 14px; height: 44px;
      border-radius: 12px; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); transition: all 0.2s;
    }
    .input-wrap:focus-within { border-color: rgba(59, 130, 246, 0.4); box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }
    .input-icon { color: #475569; font-size: 18px; width: 18px; height: 18px; }
    .input-wrap input { flex: 1; background: none; border: none; outline: none; color: #f1f5f9; font-size: 14px; font-family: inherit; }
    .input-wrap input::placeholder { color: #475569; }

    .role-options { display: flex; gap: 8px; }
    .role-option {
      flex: 1; display: flex; align-items: center; gap: 6px; justify-content: center;
      padding: 10px; border-radius: 12px; cursor: pointer; font-size: 12px; font-weight: 500;
      background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
      color: #64748b; transition: all 0.2s;
    }
    .role-option input { display: none; }
    .role-option mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .role-option.selected { border-color: rgba(59, 130, 246, 0.4); color: #3b82f6; background: rgba(59, 130, 246, 0.06); }

    .submit-btn {
      width: 100%; height: 48px; border: none; border-radius: 12px;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6); color: white;
      font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s;
      box-shadow: 0 4px 20px rgba(59, 130, 246, 0.3); margin-top: 8px;
    }
    .submit-btn:hover:not(:disabled) { box-shadow: 0 6px 30px rgba(59, 130, 246, 0.45); transform: translateY(-1px); }
    .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .login-link { text-align: center; font-size: 13px; color: #64748b; margin-top: 20px; }
    .login-link a { color: #3b82f6; font-weight: 600; text-decoration: none; }
  `]
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  roles = [
    { value: 'POLICYHOLDER', label: 'Holder', icon: 'person' },
    { value: 'ADJUSTER', label: 'Adjuster', icon: 'engineering' },
    { value: 'UNDERWRITER', label: 'Writer', icon: 'analytics' },
  ];

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router, private snackBar: MatSnackBar) {
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      role: ['POLICYHOLDER']
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;
    this.loading = true;
    this.authService.register(this.registerForm.value).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading = false;
        this.snackBar.open(err.error?.message || 'Registration failed', 'Close', { duration: 3000 });
      }
    });
  }
}

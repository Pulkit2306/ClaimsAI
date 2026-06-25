import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ClaimService } from '@core/services/claim.service';
import { ClaimType } from '@core/models/claim.model';

@Component({
  selector: 'app-claim-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule, MatSnackBarModule],
  template: `
    <div class="form-page">
      <div class="form-container glass-card">
        <div class="form-header">
          <mat-icon>assignment_add</mat-icon>
          <div>
            <h2>Submit New Claim</h2>
            <p>Fill in the details to submit an insurance claim</p>
          </div>
        </div>

        <form [formGroup]="claimForm" (ngSubmit)="onSubmit()">
          <div class="row">
            <div class="form-group">
              <label>Policy ID</label>
              <div class="input-wrap">
                <input formControlName="policyId" type="number" placeholder="Enter policy ID">
              </div>
            </div>
            <div class="form-group">
              <label>Customer ID</label>
              <div class="input-wrap">
                <input formControlName="customerId" type="number" placeholder="Enter customer ID">
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Claim Type</label>
            <div class="type-grid">
              <label *ngFor="let type of claimTypes" class="type-option"
                     [class.selected]="claimForm.get('claimType')?.value === type">
                <input type="radio" formControlName="claimType" [value]="type">
                <span>{{ type.replace('_', ' ') }}</span>
              </label>
            </div>
          </div>

          <div class="row">
            <div class="form-group">
              <label>Incident Date</label>
              <div class="input-wrap">
                <input formControlName="incidentDate" type="date">
              </div>
            </div>
            <div class="form-group">
              <label>Estimated Amount (CAD)</label>
              <div class="input-wrap">
                <span class="prefix">\$</span>
                <input formControlName="estimatedAmount" type="number" placeholder="0.00">
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Description</label>
            <div class="textarea-wrap">
              <textarea formControlName="description" rows="5"
                        placeholder="Describe the incident in detail..."></textarea>
            </div>
          </div>

          <div class="actions">
            <button type="button" class="cancel-btn" (click)="cancel()">Cancel</button>
            <button type="submit" class="submit-btn" [disabled]="claimForm.invalid || loading">
              {{ loading ? 'Submitting...' : 'Submit Claim' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .form-page { padding: 32px; max-width: 720px; margin: 0 auto; }
    .form-container { padding: 32px; }
    .form-header { display: flex; align-items: center; gap: 14px; margin-bottom: 28px; }
    .form-header mat-icon { font-size: 28px; width: 28px; height: 28px; color: #3b82f6; }
    .form-header h2 { font-size: 22px; font-weight: 700; }
    .form-header p { font-size: 13px; color: #64748b; }

    .row { display: flex; gap: 14px; }
    .row .form-group { flex: 1; }
    .form-group { margin-bottom: 18px; }
    .form-group > label { display: block; font-size: 12px; font-weight: 600; color: #94a3b8; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.04em; }
    .input-wrap {
      display: flex; align-items: center; gap: 8px; padding: 0 14px; height: 44px;
      border-radius: 12px; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); transition: all 0.2s;
    }
    .input-wrap:focus-within { border-color: rgba(59,130,246,0.4); box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
    .input-wrap input { flex: 1; background: none; border: none; outline: none; color: #f1f5f9; font-size: 14px; font-family: inherit; }
    .input-wrap input::placeholder { color: #475569; }
    .prefix { color: #64748b; font-weight: 600; }

    .textarea-wrap {
      border-radius: 12px; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); transition: all 0.2s;
    }
    .textarea-wrap:focus-within { border-color: rgba(59,130,246,0.4); box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
    .textarea-wrap textarea {
      width: 100%; background: none; border: none; outline: none; resize: vertical;
      color: #f1f5f9; font-size: 14px; font-family: inherit; padding: 12px 14px;
    }
    .textarea-wrap textarea::placeholder { color: #475569; }

    .type-grid { display: flex; flex-wrap: wrap; gap: 6px; }
    .type-option {
      padding: 6px 14px; border-radius: 10px; font-size: 11px; font-weight: 500; cursor: pointer;
      background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
      color: #64748b; transition: all 0.2s;
    }
    .type-option input { display: none; }
    .type-option.selected { border-color: rgba(59,130,246,0.4); color: #3b82f6; background: rgba(59,130,246,0.08); }

    .actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }
    .cancel-btn {
      padding: 10px 24px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.08);
      background: transparent; color: #94a3b8; font-size: 14px; font-weight: 500;
      cursor: pointer; transition: all 0.2s; font-family: inherit;
    }
    .cancel-btn:hover { background: rgba(255,255,255,0.04); }
    .submit-btn {
      padding: 10px 24px; border-radius: 12px; border: none;
      background: linear-gradient(135deg, #3b82f6, #8b5cf6); color: white;
      font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s;
      box-shadow: 0 4px 15px rgba(59,130,246,0.3); font-family: inherit;
    }
    .submit-btn:hover:not(:disabled) { box-shadow: 0 6px 25px rgba(59,130,246,0.45); transform: translateY(-1px); }
    .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
  `]
})
export class ClaimFormComponent {
  claimForm: FormGroup;
  claimTypes = Object.values(ClaimType);
  loading = false;

  constructor(private fb: FormBuilder, private claimService: ClaimService, private router: Router, private snackBar: MatSnackBar) {
    this.claimForm = this.fb.group({
      policyId: ['', Validators.required],
      customerId: ['', Validators.required],
      claimType: ['', Validators.required],
      incidentDate: ['', Validators.required],
      estimatedAmount: [''],
      description: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  onSubmit(): void {
    if (this.claimForm.invalid) return;
    this.loading = true;
    this.claimService.createClaim(this.claimForm.value).subscribe({
      next: (response) => {
        this.snackBar.open('Claim submitted successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/claims', response.data.id]);
      },
      error: () => { this.loading = false; this.snackBar.open('Failed to submit claim', 'Close', { duration: 3000 }); }
    });
  }

  cancel(): void { this.router.navigate(['/claims']); }
}

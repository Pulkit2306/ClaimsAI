import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClaimListComponent } from './claim-list.component';
import { ClaimService } from '@core/services/claim.service';
import { RouterTestingModule } from '@angular/router/testing';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Claim } from '@core/models/claim.model';

const mockClaims: Claim[] = [
  {
    id: 1,
    claimNumber: 'CLM-ABCD1234',
    policyId: 10,
    customerId: 5,
    claimType: 'AUTO',
    status: 'SUBMITTED',
    description: 'Rear-end collision',
    incidentDate: '2025-06-01',
    estimatedAmount: 8500,
    fraudScore: 0.15
  },
  {
    id: 2,
    claimNumber: 'CLM-XYZ5678',
    policyId: 11,
    customerId: 6,
    claimType: 'HOME',
    status: 'APPROVED',
    description: 'Water damage',
    incidentDate: '2025-05-15',
    estimatedAmount: 22000,
    fraudScore: 0.82
  }
];

describe('ClaimListComponent', () => {
  let component: ClaimListComponent;
  let fixture: ComponentFixture<ClaimListComponent>;
  let claimServiceSpy: jasmine.SpyObj<ClaimService>;

  beforeEach(async () => {
    claimServiceSpy = jasmine.createSpyObj('ClaimService', ['getAllClaims']);
    claimServiceSpy.getAllClaims.and.returnValue(of({
      success: true,
      data: { content: mockClaims, totalElements: 2 },
      message: ''
    }));

    await TestBed.configureTestingModule({
      imports: [
        ClaimListComponent,
        RouterTestingModule,
        MatPaginatorModule,
        MatIconModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [{ provide: ClaimService, useValue: claimServiceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(ClaimListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load claims on init', () => {
    expect(claimServiceSpy.getAllClaims).toHaveBeenCalledOnceWith(0, 10);
    expect(component.claims).toHaveSize(2);
    expect(component.totalElements).toBe(2);
  });

  it('should render claim numbers in the table', () => {
    const rows = fixture.nativeElement.querySelectorAll('.claim-number');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent.trim()).toBe('CLM-ABCD1234');
    expect(rows[1].textContent.trim()).toBe('CLM-XYZ5678');
  });

  describe('formatType', () => {
    it('should replace underscores with spaces', () => {
      expect(component.formatType('WATER_DAMAGE')).toBe('WATER DAMAGE');
      expect(component.formatType('AUTO')).toBe('AUTO');
    });
  });

  describe('formatStatus', () => {
    it('should replace underscores with spaces', () => {
      expect(component.formatStatus('UNDER_REVIEW')).toBe('UNDER REVIEW');
      expect(component.formatStatus('FLAGGED_FRAUD')).toBe('FLAGGED FRAUD');
    });
  });

  describe('getFraudGradient', () => {
    it('should return red gradient for high fraud score', () => {
      expect(component.getFraudGradient(0.85)).toContain('#ef4444');
    });

    it('should return orange gradient for medium fraud score', () => {
      expect(component.getFraudGradient(0.5)).toContain('#f97316');
    });

    it('should return green gradient for low fraud score', () => {
      expect(component.getFraudGradient(0.2)).toContain('#10b981');
    });
  });

  describe('onPage', () => {
    it('should reload claims with new page params', () => {
      const pageEvent: PageEvent = { pageIndex: 1, pageSize: 5, length: 20 };
      component.onPage(pageEvent);
      expect(claimServiceSpy.getAllClaims).toHaveBeenCalledWith(1, 5);
    });
  });
});

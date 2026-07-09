import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClaimService } from './claim.service';
import { Claim, ClaimStats } from '../models/claim.model';
import { ApiResponse } from '../models/user.model';

describe('ClaimService', () => {
  let service: ClaimService;
  let httpMock: HttpTestingController;

  const mockClaim: Claim = {
    id: 1,
    claimNumber: 'CLM-ABCD1234',
    policyId: 10,
    customerId: 5,
    claimType: 'AUTO',
    status: 'SUBMITTED',
    description: 'Rear-end collision',
    incidentDate: '2025-06-01',
    estimatedAmount: 8500,
    fraudScore: 0.1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClaimService]
    });
    service = TestBed.inject(ClaimService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllClaims', () => {
    it('should GET paginated claims with correct params', () => {
      service.getAllClaims(0, 10).subscribe();

      const req = httpMock.expectOne(r => r.url.includes('/claims') && r.params.get('page') === '0');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('size')).toBe('10');
      req.flush({ success: true, data: { content: [mockClaim] } });
    });
  });

  describe('getClaimById', () => {
    it('should GET claim by id', () => {
      const apiResponse: ApiResponse<Claim> = { success: true, data: mockClaim, message: '' };
      service.getClaimById(1).subscribe(res => {
        expect(res.data.claimNumber).toBe('CLM-ABCD1234');
      });

      const req = httpMock.expectOne(r => r.url.includes('/claims/1'));
      expect(req.request.method).toBe('GET');
      req.flush(apiResponse);
    });
  });

  describe('createClaim', () => {
    it('should POST new claim', () => {
      const request = {
        policyId: 10,
        customerId: 5,
        claimType: 'AUTO',
        description: 'Rear-end collision',
        incidentDate: '2025-06-01',
        estimatedAmount: 8500
      };
      const apiResponse: ApiResponse<Claim> = { success: true, data: mockClaim, message: '' };

      service.createClaim(request as any).subscribe(res => {
        expect(res.data.id).toBe(1);
      });

      const req = httpMock.expectOne(r => r.url.includes('/claims') && r.method === 'POST');
      expect(req.request.body).toEqual(request);
      req.flush(apiResponse);
    });
  });

  describe('updateClaimStatus', () => {
    it('should PATCH claim status', () => {
      const apiResponse: ApiResponse<Claim> = {
        success: true,
        data: { ...mockClaim, status: 'UNDER_REVIEW' },
        message: ''
      };

      service.updateClaimStatus(1, 'UNDER_REVIEW').subscribe(res => {
        expect(res.data.status).toBe('UNDER_REVIEW');
      });

      const req = httpMock.expectOne(r => r.url.includes('/claims/1/status'));
      expect(req.request.method).toBe('PATCH');
      req.flush(apiResponse);
    });
  });

  describe('getClaimStats', () => {
    it('should GET claim statistics', () => {
      const stats: ClaimStats = { total: 12, submitted: 4, underReview: 3, approved: 3, denied: 1, flaggedFraud: 1 };
      const apiResponse: ApiResponse<ClaimStats> = { success: true, data: stats, message: '' };

      service.getClaimStats().subscribe(res => {
        expect(res.data.total).toBe(12);
        expect(res.data.approved).toBe(3);
      });

      const req = httpMock.expectOne(r => r.url.includes('/claims/stats'));
      expect(req.request.method).toBe('GET');
      req.flush(apiResponse);
    });
  });

  describe('getSuspiciousClaims', () => {
    it('should GET suspicious claims with threshold param', () => {
      service.getSuspiciousClaims(0.7).subscribe();

      const req = httpMock.expectOne(r => r.url.includes('/claims/suspicious'));
      expect(req.request.params.get('threshold')).toBe('0.7');
      req.flush({ success: true, data: [] });
    });
  });

  describe('assignAdjuster', () => {
    it('should PATCH adjuster assignment', () => {
      service.assignAdjuster(1, 'adjuster-007').subscribe();

      const req = httpMock.expectOne(r => r.url.includes('/claims/1/assign'));
      expect(req.request.method).toBe('PATCH');
      req.flush({ success: true, data: mockClaim });
    });
  });
});

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { ApiResponse, AuthResponse } from '../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockAuthResponse: AuthResponse = {
    accessToken: 'mock-jwt-token',
    tokenType: 'Bearer',
    expiresIn: 86400,
    role: 'ADJUSTER',
    fullName: 'John Doe'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should POST credentials and store token on success', () => {
      const apiResponse: ApiResponse<AuthResponse> = {
        success: true,
        data: mockAuthResponse,
        message: 'Login successful'
      };

      service.login({ email: 'john@example.com', password: 'pass123' }).subscribe(res => {
        expect(res.success).toBeTrue();
        expect(res.data.accessToken).toBe('mock-jwt-token');
      });

      const req = httpMock.expectOne(r => r.url.includes('/auth/login'));
      expect(req.request.method).toBe('POST');
      req.flush(apiResponse);

      expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('should not store token when success is false', () => {
      const apiResponse: ApiResponse<AuthResponse> = {
        success: false,
        data: null as any,
        message: 'Invalid credentials'
      };

      service.login({ email: 'bad@example.com', password: 'wrong' }).subscribe();

      const req = httpMock.expectOne(r => r.url.includes('/auth/login'));
      req.flush(apiResponse);

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(service.isAuthenticated()).toBeFalse();
    });
  });

  describe('logout', () => {
    it('should clear storage and nullify currentUser$', () => {
      localStorage.setItem('auth_token', 'some-token');
      localStorage.setItem('auth_user', JSON.stringify(mockAuthResponse));

      service.logout();

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(service.isAuthenticated()).toBeFalse();
      expect(service.getCurrentUser()).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return false when no token stored', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });

    it('should return true when token is present', () => {
      localStorage.setItem('auth_token', 'valid-token');
      expect(service.isAuthenticated()).toBeTrue();
    });
  });

  describe('getCurrentUser', () => {
    it('should return null when no user stored', () => {
      expect(service.getCurrentUser()).toBeNull();
    });
  });

  describe('register', () => {
    it('should POST registration request and store auth data', () => {
      const apiResponse: ApiResponse<AuthResponse> = {
        success: true,
        data: mockAuthResponse,
        message: 'Registered successfully'
      };

      service.register({
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane@example.com',
        password: 'pass',
        role: 'POLICYHOLDER'
      }).subscribe();

      const req = httpMock.expectOne(r => r.url.includes('/auth/register'));
      expect(req.request.method).toBe('POST');
      req.flush(apiResponse);

      expect(localStorage.getItem('auth_token')).toBe('mock-jwt-token');
    });
  });
});

export interface Policy {
  id: number;
  policyNumber: string;
  policyType: PolicyType;
  customerId: number;
  premiumAmount: number;
  coverageAmount: number;
  deductible: number;
  startDate: string;
  endDate: string;
  active: boolean;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export enum PolicyType {
  AUTO = 'AUTO',
  HOME = 'HOME',
  HEALTH = 'HEALTH',
  LIFE = 'LIFE',
  COMMERCIAL = 'COMMERCIAL',
  TRAVEL = 'TRAVEL'
}

export interface PolicyRequest {
  customerId: number;
  policyType: string;
  premiumAmount: number;
  coverageAmount: number;
  deductible: number;
  startDate: string;
  endDate: string;
  description?: string;
}

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  province: string;
  postalCode: string;
  dateOfBirth: string;
}

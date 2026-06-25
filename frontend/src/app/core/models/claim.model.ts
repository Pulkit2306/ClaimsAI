export interface Claim {
  id: number;
  claimNumber: string;
  policyId: number;
  customerId: number;
  claimType: ClaimType;
  status: ClaimStatus;
  description: string;
  incidentDate: string;
  estimatedAmount: number;
  approvedAmount: number;
  assignedAdjusterId: string;
  fraudScore: number;
  aiSummary: string;
  documentUrls: string[];
  createdAt: string;
  updatedAt: string;
}

export enum ClaimStatus {
  SUBMITTED = 'SUBMITTED',
  UNDER_REVIEW = 'UNDER_REVIEW',
  AI_PROCESSING = 'AI_PROCESSING',
  ADJUSTER_ASSIGNED = 'ADJUSTER_ASSIGNED',
  INVESTIGATION = 'INVESTIGATION',
  APPROVED = 'APPROVED',
  DENIED = 'DENIED',
  SETTLED = 'SETTLED',
  CLOSED = 'CLOSED',
  FLAGGED_FRAUD = 'FLAGGED_FRAUD'
}

export enum ClaimType {
  AUTO_COLLISION = 'AUTO_COLLISION',
  AUTO_THEFT = 'AUTO_THEFT',
  HOME_FIRE = 'HOME_FIRE',
  HOME_WATER_DAMAGE = 'HOME_WATER_DAMAGE',
  HOME_THEFT = 'HOME_THEFT',
  HOME_WEATHER = 'HOME_WEATHER',
  HEALTH_MEDICAL = 'HEALTH_MEDICAL',
  HEALTH_DENTAL = 'HEALTH_DENTAL',
  LIABILITY = 'LIABILITY',
  OTHER = 'OTHER'
}

export interface ClaimRequest {
  policyId: number;
  customerId: number;
  claimType: string;
  description: string;
  incidentDate: string;
  estimatedAmount?: number;
  documentUrls?: string[];
}

export interface ClaimStats {
  total: number;
  submitted: number;
  underReview: number;
  approved: number;
  denied: number;
  flaggedFraud: number;
}

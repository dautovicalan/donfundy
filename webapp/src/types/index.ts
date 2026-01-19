export enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER',
}

export enum Status {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export enum PaymentMethod {
  CARD = 'CARD',
  BANK_TRANSFER = 'BANK_TRANSFER',
  PAYPAL = 'PAYPAL',
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  repeatPassword: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  email: string;
  role: string;
}

export interface CampaignRequest {
  name: string;
  description?: string;
  goalAmount: number;
  startDate: string;
  endDate?: string;
  status: Status;
}

export interface CampaignResponse {
  id: number;
  name: string;
  description?: string;
  goalAmount: number;
  raisedAmount: number;
  startDate: string;
  endDate?: string;
  status: Status;
  progressPercentage: number;
  createdById?: number;
  createdByName?: string;
  createdByEmail?: string;
}

export interface DonorRequest {
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
}

export interface DonorResponse {
  id: number;
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
}

export interface DonationRequest {
  campaignId: number;
  donorId: number;
  amount: number;
  message?: string;
  paymentMethod: PaymentMethod;
}

export interface DonationResponse {
  id: number;
  campaignId: number;
  campaignName: string;
  donorId: number;
  donorName: string;
  amount: number;
  donationDate: string;
  message?: string;
  paymentMethod: PaymentMethod;
}

export interface ErrorResponse {
  message: string;
}

export interface BulkDonationResult {
  totalRows: number;
  successCount: number;
  failureCount: number;
  errors: string[];
}

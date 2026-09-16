export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export type AustralianState = 'NSW' | 'VIC' | 'QLD' | 'SA' | 'WA' | 'TAS' | 'NT' | 'ACT';

export const GENDERS: Gender[] = ['MALE', 'FEMALE', 'OTHER'];

export const STATES: AustralianState[] = ['NSW', 'VIC', 'QLD', 'SA', 'WA', 'TAS', 'NT', 'ACT'];

export interface Address {
  addressLine: string;
  suburb: string;
  state: AustralianState;
  postcode: string;
}

export interface Patient {
  id?: number;
  pid: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;      // ISO format: 1990-01-15
  gender: Gender;
  phoneNo: string;
  address: Address;
}

/** Mirrors the PageResponse record on the backend. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Error payload returned by GlobalExceptionHandler. */
export interface ApiError {
  status: number;
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
  timestamp: string;
}
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;      
  role: UserRole;          
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;   
  confirmPassword: string;
}

export interface ResendVerificationRequest {
  email: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: string;
  message: string;
}

export type UserRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';
export type UserRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    role: UserRole;
}

export interface CreateUserByAdminRequest {
    email: string;
    password: string;
    role: UserRole;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    email: string;
    role: UserRole;
    message?: string;
}

export interface ForgotPasswordRequest {
    email: string;
}

export interface ResetPasswordRequest {
    token: string;
    newPassword: string;
    confirmPassword: string;
}

export interface SetPasswordRequest {
    token: string;
    password: string;
    confirmPassword: string;
}

export interface ResendVerificationRequest {
    email: string;
}

export interface AuthenticatedUser {
    email: string;
    role: UserRole;
    accessToken: string;
    refreshToken: string;
}

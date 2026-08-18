import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '@/environments/env';
import {
    AuthenticatedUser,
    AuthResponse,
    CreateUserByAdminRequest,
    ForgotPasswordRequest,
    LoginRequest,
    RegisterRequest,
    ResendVerificationRequest,
    ResetPasswordRequest,
    SetPasswordRequest
} from '../dto/auth.dto';
import { toApiError } from '../dto/api-error.dto';

const AUTH_STORAGE_KEY = 'hrflow_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly router = inject(Router);

    private readonly _user = signal<AuthenticatedUser | null>(this.loadUser());

    readonly user = computed(() => this._user());
    readonly isAuthenticated = computed(() => !!this._user());
    readonly userRole = computed(() => this._user()?.role ?? null);

    login(credentials: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.login, credentials).pipe(
            tap((response) => this.storeUser(response)),
            catchError((error) => this.handleError(error, 'Login failed'))
        );
    }

    register(payload: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.register, payload).pipe(
            catchError((error) => this.handleError(error, 'Registration failed'))
        );
    }

    createUserByAdmin(payload: CreateUserByAdminRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.createUserByAdmin, payload).pipe(
            catchError((error) => this.handleError(error, 'User creation failed'))
        );
    }

    forgotPassword(payload: ForgotPasswordRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.forgotPassword, payload).pipe(
            catchError((error) => this.handleError(error, 'Password reset request failed'))
        );
    }

    resetPassword(payload: ResetPasswordRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.resetPassword, payload).pipe(
            catchError((error) => this.handleError(error, 'Password reset failed'))
        );
    }

    setPassword(payload: SetPasswordRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.setPassword, payload).pipe(
            catchError((error) => this.handleError(error, 'Password setup failed'))
        );
    }

    resendActivation(payload: ResendVerificationRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(environment.authEndpoints.resendActivation, payload).pipe(
            catchError((error) => this.handleError(error, 'Resend activation failed'))
        );
    }

    validateActivationToken(token: string): Observable<void> {
        return this.http.get<void>(environment.authEndpoints.validateActivationToken, { params: { token } }).pipe(
            catchError((error) => this.handleError(error, 'Invalid or expired activation token'))
        );
    }

    logout(): void {
        this._user.set(null);
        localStorage.removeItem(AUTH_STORAGE_KEY);
        this.router.navigate(['/auth/login']);
    }

    getAccessToken(): string | null {
        return this._user()?.accessToken ?? null;
    }

    hasRole(role: string): boolean {
        return this._user()?.role === role;
    }

    private storeUser(response: AuthResponse): void {
        const user: AuthenticatedUser = {
            email: response.email,
            role: response.role,
            accessToken: response.accessToken,
            refreshToken: response.refreshToken
        };
        this._user.set(user);
        localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
    }

    private loadUser(): AuthenticatedUser | null {
        try {
            const raw = localStorage.getItem(AUTH_STORAGE_KEY);
            if (!raw) return null;
            return JSON.parse(raw) as AuthenticatedUser;
        } catch {
            return null;
        }
    }

    private handleError(error: unknown, defaultMessage: string): Observable<never> {
        const apiError = error instanceof HttpErrorResponse ? toApiError(error.error) : null;
        const message = apiError?.message ?? (error instanceof Error ? error.message : defaultMessage);
        return throwError(() => new Error(message));
    }
}

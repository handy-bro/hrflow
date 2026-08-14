import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  LoginRequest,
  RegisterRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  ResendVerificationRequest,
  AuthResponse,
} from '../models/auth.model';

const TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  // ─── State (signals) ──────────────────────────────────────────────────────
  private _currentUser = signal<AuthResponse | null>(this._loadUser());
  readonly currentUser = this._currentUser.asReadonly();
  readonly isLoggedIn = computed(() => !!this._currentUser());
  readonly userRole = computed(() => this._currentUser()?.role ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  private _authEndpoints = environment.authEnpoints;

  // ─── Auth calls ───────────────────────────────────────────────────────────

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this._authEndpoints.login, payload).pipe(
      tap(res => this._persist(res))
    );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this._authEndpoints.register, payload).pipe(
      tap(res => this._persist(res))
    );
  }

  forgotPassword(payload: ForgotPasswordRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this._authEndpoints.forgotPassword, payload);
  }

  resetPassword(payload: ResetPasswordRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this._authEndpoints.resetPassword, payload);
  }

  verifyEmail(token: string): Observable<string> {
    return this.http.get<string>(this._authEndpoints.verifyEmail, { params: { token } });
  }

  resendVerification(payload: ResendVerificationRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this._authEndpoints.resendVerification, payload);
  }

  // ─── Session ──────────────────────────────────────────────────────────────

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private _persist(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(res));
    this._currentUser.set(res);
  }

  private _loadUser(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
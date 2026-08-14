import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// ─── Protect private routes ───────────────────────────────────────────────
export const authGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) return true;

  router.navigate(['/auth/login']);
  return false;
};

// ─── Redirect logged-in users away from /auth pages ──────────────────────
export const guestGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) return true;

  router.navigate(['/dashboard']);
  return false;
};

// ─── Role-based access ────────────────────────────────────────────────────
export const roleGuard = (allowedRoles: string[]): CanActivateFn => () => {
  const auth   = inject(AuthService);
  const router = inject(Router);
  const role   = auth.userRole();

  if (role && allowedRoles.includes(role)) return true;

  router.navigate(['/unauthorized']);
  return false;
};
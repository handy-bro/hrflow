import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const APP_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    // Auth pages — lazy loaded
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },
  
//   {
//     // Protected Admin zone
//     path: 'dashboard',
//     canActivate: [authGuard],
//     loadComponent: () =>
//       import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
//   },
//   {
//     path: 'unauthorized',
//     loadComponent: () =>
//       import('./shared/components/unauthorized/unauthorized.component')
//         .then(m => m.UnauthorizedComponent),
//   },
  {
    path: '**',
    redirectTo: 'auth/login',
  },
];
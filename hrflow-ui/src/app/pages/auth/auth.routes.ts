import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { SetPasswordComponent } from './set-password/set-password.component';
import { publicGuard } from '@/app/core/guards/auth.guard';

export default [
    { path: 'login', component: LoginComponent, canActivate: [publicGuard] },
    // { path: 'register', component: RegisterComponent, canActivate: [publicGuard] },
    { path: 'forgot-password', component: ForgotPasswordComponent, canActivate: [publicGuard] },
    { path: 'reset-password', component: ResetPasswordComponent, canActivate: [publicGuard] },
    { path: 'set-password', component: SetPasswordComponent, canActivate: [publicGuard] }
] as Routes;

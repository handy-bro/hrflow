import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { AuthCardComponent } from '../components/auth-card.component';
import { AuthService } from '@/app/core/services/auth.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { ResetPasswordRequest } from '@/app/core/dto/auth.dto';

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [ButtonModule, PasswordModule, FormsModule, RouterModule, RippleModule, ToastModule, AuthCardComponent],
    templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    private readonly errorHandler = inject(ErrorHandlerService);

    readonly payload = signal<ResetPasswordRequest>({ token: '', newPassword: '', confirmPassword: '' });
    readonly loading = signal<boolean>(false);
    readonly success = signal<boolean>(false);

    updateNewPassword(newPassword: string): void {
        this.payload.update((p) => ({ ...p, newPassword }));
    }

    updateConfirmPassword(confirmPassword: string): void {
        this.payload.update((p) => ({ ...p, confirmPassword }));
    }

    ngOnInit(): void {
        const token = this.route.snapshot.queryParamMap.get('token');
        if (!token) {
            this.errorHandler.handleHttpErrorAndThrow({ code: 'MISSING_TOKEN', message: 'No reset token provided.', status: '400 BAD_REQUEST', path: '/auth/reset-password' });
        }
        this.payload.update((state) => ({ ...state, token: token ?? '' }));
    }

    onSubmit(): void {
        const { newPassword, confirmPassword } = this.payload();
        if (!newPassword || !confirmPassword) {
            this.errorHandler.showWarning('Validation', 'Please fill in all fields.');
            return;
        }
        if (newPassword.length < 8) {
            this.errorHandler.showWarning('Validation', 'Password must be at least 8 characters.');
            return;
        }
        if (newPassword !== confirmPassword) {
            this.errorHandler.showWarning('Validation', 'Passwords do not match.');
            return;
        }

        this.loading.set(true);
        this.authService.resetPassword(this.payload()).subscribe({
            next: () => {
                this.loading.set(false);
                this.success.set(true);
                this.errorHandler.showSuccess('Password reset', 'Your password has been reset. You can now log in.');
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
}

import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { AuthCardComponent } from '../components/auth-card.component';
import { AuthService } from '@/app/core/services/auth.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { SetPasswordRequest } from '@/app/core/dto/auth.dto';

@Component({
    selector: 'app-set-password',
    standalone: true,
    imports: [ButtonModule, PasswordModule, FormsModule, RouterModule, RippleModule, ToastModule, AuthCardComponent],
    templateUrl: './set-password.component.html'
})
export class SetPasswordComponent implements OnInit {
    private readonly authService = inject(AuthService);
    private readonly route = inject(ActivatedRoute);
    private readonly errorHandler = inject(ErrorHandlerService);

    readonly payload = signal<SetPasswordRequest>({ token: '', password: '', confirmPassword: '' });
    readonly loading = signal<boolean>(false);
    readonly success = signal<boolean>(false);
    readonly tokenValid = signal<boolean>(true);

    updatePassword(password: string): void {
        this.payload.update((p) => ({ ...p, password }));
    }

    updateConfirmPassword(confirmPassword: string): void {
        this.payload.update((p) => ({ ...p, confirmPassword }));
    }

    ngOnInit(): void {
        const token = this.route.snapshot.queryParamMap.get('token');
        if (!token) {
            this.tokenValid.set(false);
            this.errorHandler.handleHttpError({ code: 'MISSING_TOKEN', message: 'No activation token provided.', status: '400 BAD_REQUEST', path: '/auth/set-password' });
            return;
        }
        this.payload.update((state) => ({ ...state, token }));
        this.authService.validateActivationToken(token).subscribe({
            error: () => {
                this.tokenValid.set(false);
            }
        });
    }

    onSubmit(): void {
        const { password, confirmPassword } = this.payload();
        if (!password || !confirmPassword) {
            this.errorHandler.showWarning('Validation', 'Please fill in all fields.');
            return;
        }
        if (password.length < 8) {
            this.errorHandler.showWarning('Validation', 'Password must be at least 8 characters.');
            return;
        }
        if (password !== confirmPassword) {
            this.errorHandler.showWarning('Validation', 'Passwords do not match.');
            return;
        }

        this.loading.set(true);
        this.authService.setPassword(this.payload()).subscribe({
            next: () => {
                this.loading.set(false);
                this.success.set(true);
                this.errorHandler.showSuccess('Account activated', 'Your password has been set. You can now log in.');
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
}

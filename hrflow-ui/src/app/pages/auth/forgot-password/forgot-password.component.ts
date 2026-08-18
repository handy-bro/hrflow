import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { AuthCardComponent } from '../components/auth-card.component';
import { AuthService } from '@/app/core/services/auth.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { ForgotPasswordRequest } from '@/app/core/dto/auth.dto';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [ButtonModule, InputTextModule, FormsModule, RouterModule, RippleModule, ToastModule, AuthCardComponent],
    templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
    private readonly authService = inject(AuthService);
    private readonly errorHandler = inject(ErrorHandlerService);

    readonly payload = signal<ForgotPasswordRequest>({ email: '' });
    readonly loading = signal<boolean>(false);
    readonly sent = signal<boolean>(false);

    updateEmail(email: string): void {
        this.payload.update((p) => ({ ...p, email }));
    }

    onSubmit(): void {
        const { email } = this.payload();
        if (!email) {
            this.errorHandler.showWarning('Validation', 'Please enter your email address.');
            return;
        }

        this.loading.set(true);
        this.authService.forgotPassword(this.payload()).subscribe({
            next: () => {
                this.loading.set(false);
                this.sent.set(true);
                this.errorHandler.showSuccess('Email sent', 'Check your inbox for reset instructions.');
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
}

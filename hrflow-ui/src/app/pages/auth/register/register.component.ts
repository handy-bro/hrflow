import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { SelectModule } from 'primeng/select';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { AuthCardComponent } from '../components/auth-card.component';
import { AuthService } from '@/app/core/services/auth.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { RegisterRequest, UserRole } from '@/app/core/dto/auth.dto';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [ButtonModule, InputTextModule, PasswordModule, SelectModule, FormsModule, RouterModule, RippleModule, ToastModule, AuthCardComponent],
    templateUrl: './register.component.html'
})
export class RegisterComponent {
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);
    private readonly errorHandler = inject(ErrorHandlerService);

    readonly payload = signal<RegisterRequest>({ email: '', password: '', role: 'EMPLOYEE' });
    readonly confirmPassword = signal<string>('');
    readonly loading = signal<boolean>(false);

    readonly roles: { label: string; value: UserRole }[] = [
        { label: 'Employee', value: 'EMPLOYEE' },
        { label: 'Manager', value: 'MANAGER' },
        { label: 'Admin', value: 'ADMIN' }
    ];

    updateEmail(email: string): void {
        this.payload.update((p) => ({ ...p, email }));
    }

    updateRole(role: UserRole): void {
        this.payload.update((p) => ({ ...p, role }));
    }

    updatePassword(password: string): void {
        this.payload.update((p) => ({ ...p, password }));
    }

    updateConfirmPassword(confirmPassword: string): void {
        this.confirmPassword.set(confirmPassword);
    }

    onSubmit(): void {
        const { email, password, role } = this.payload();
        if (!email || !password || !role) {
            this.errorHandler.showWarning('Validation', 'Please fill in all fields.');
            return;
        }
        if (password.length < 8) {
            this.errorHandler.showWarning('Validation', 'Password must be at least 8 characters.');
            return;
        }
        if (password !== this.confirmPassword()) {
            this.errorHandler.showWarning('Validation', 'Passwords do not match.');
            return;
        }

        this.loading.set(true);
        this.authService.register(this.payload()).subscribe({
            next: () => {
                this.errorHandler.showSuccess('Account created', 'Please check your email to activate your account.');
                this.router.navigate(['/auth/login']);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
}

import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { AuthCardComponent } from '../components/auth-card.component';
import { AuthService } from '@/app/core/services/auth.service';
import { LoginRequest } from '@/app/core/dto/auth.dto';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [
        ButtonModule,
        CheckboxModule,
        InputTextModule,
        PasswordModule,
        FormsModule,
        RouterModule,
        RippleModule,
        ToastModule,
        AuthCardComponent
    ],
    templateUrl: './login.component.html'
})
export class LoginComponent {
    private readonly authService = inject(AuthService);
    private readonly router = inject(Router);

    readonly credentials = signal<LoginRequest>({ email: '', password: '' });
    readonly rememberMe = signal<boolean>(false);
    readonly loading = signal<boolean>(false);

    readonly userEmail = computed(() => this.credentials().email);

    updateEmail(email: string): void {
        this.credentials.update((c) => ({ ...c, email }));
    }

    updatePassword(password: string): void {
        this.credentials.update((c) => ({ ...c, password }));
    }

    onSubmit(): void {
        const { email, password } = this.credentials();
        if (!email || !password) {
            return;
        }

        this.loading.set(true);
        this.authService.login(this.credentials()).subscribe({
            next: () => {
                this.router.navigate(['/']);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
}

import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, PasswordModule, ButtonModule, 
    CheckboxModule, MessageModule, ProgressSpinnerModule
  ],
  template: `
    <div class="animate-slide-up">
      <!-- Header -->
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-secondary-900 mb-2">Connexion</h2>
        <p class="text-secondary-500">Bienvenue ! Connectez-vous à votre compte.</p>
      </div>

      <!-- Form -->
      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="space-y-5">
        <!-- Email -->
        <div class="space-y-2">
          <label class="text-sm font-medium text-secondary-700 block">Adresse email</label>
          <span class="p-input-icon-left w-full">
            <i class="pi pi-envelope text-secondary-400"></i>
            <input 
              pInputText 
              type="email" 
              formControlName="email"
              placeholder="exemple@entreprise.com"
              class="w-full"
              [ngClass]="{'ng-invalid ng-dirty': loginForm.get('email')?.invalid && loginForm.get('email')?.touched}"
            />
          </span>
          @if (loginForm.get('email')?.invalid && loginForm.get('email')?.touched) {
            <small class="text-danger-500 text-xs">Veuillez entrer une adresse email valide</small>
          }
        </div>

        <!-- Password -->
        <div class="space-y-2">
          <label class="text-sm font-medium text-secondary-700 block">Mot de passe</label>
          <p-password 
            formControlName="password"
            [toggleMask]="true"
            [feedback]="false"
            placeholder="Votre mot de passe"
            styleClass="w-full"
            inputStyleClass="w-full"
            [ngClass]="{'ng-invalid ng-dirty': loginForm.get('password')?.invalid && loginForm.get('password')?.touched}"
          />
          @if (loginForm.get('password')?.invalid && loginForm.get('password')?.touched) {
            <small class="text-danger-500 text-xs">Le mot de passe est requis</small>
          }
        </div>

        <!-- Remember & Forgot -->
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <p-checkbox 
              formControlName="rememberMe" 
              [binary]="true"
              inputId="remember"
            />
            <label for="remember" class="text-sm text-secondary-600 cursor-pointer">Se souvenir de moi</label>
          </div>
          <a routerLink="/auth/forgot-password" class="text-sm text-primary-600 hover:text-primary-700 font-medium transition-colors">
            Mot de passe oublié ?
          </a>
        </div>

        <!-- Error Message -->
        @if (errorMessage()) {
          <p-message severity="error" [text]="errorMessage()" styleClass="w-full"></p-message>
        }

        <!-- Submit Button -->
        <button 
          pButton 
          type="submit" 
          label="Se connecter"
          class="w-full py-3"
          [loading]="authService.isLoading()"
          [disabled]="loginForm.invalid"
        ></button>

        <!-- Divider -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-secondary-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-2 bg-secondary-50 text-secondary-500">ou</span>
          </div>
        </div>

        <!-- Social Login -->
        <div class="grid grid-cols-2 gap-3">
          <button 
            pButton 
            type="button"
            icon="pi pi-google"
            label="Google"
            class="p-button-outlined w-full"
            severity="secondary"
          ></button>
          <button 
            pButton 
            type="button"
            icon="pi pi-microsoft"
            label="Microsoft"
            class="p-button-outlined w-full"
            severity="secondary"
          ></button>
        </div>
      </form>

      <!-- Register Link -->
      <p class="text-center mt-8 text-sm text-secondary-600">
        Pas encore de compte ? 
        <a routerLink="/auth/register" class="text-primary-600 hover:text-primary-700 font-semibold transition-colors">
          Créer un compte
        </a>
      </p>
    </div>
  `,
  styles: []
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    public authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.errorMessage.set(null);

    this.authService.login(this.loginForm.value).subscribe({
      error: (err) => {
        this.errorMessage.set(err.message || 'Erreur de connexion');
      }
    });
  }
}

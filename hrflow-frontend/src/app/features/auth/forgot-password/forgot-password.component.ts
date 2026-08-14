import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, ButtonModule, MessageModule
  ],
  template: `
    <div class="animate-slide-up">
      <div class="text-center mb-8">
        <div class="w-16 h-16 mx-auto mb-4 rounded-2xl bg-primary-100 flex items-center justify-center">
          <i class="pi pi-lock-open text-2xl text-primary-600"></i>
        </div>
        <h2 class="text-3xl font-bold text-secondary-900 mb-2">Mot de passe oublié ?</h2>
        <p class="text-secondary-500">Entrez votre email pour recevoir un lien de réinitialisation.</p>
      </div>

      @if (!emailSent()) {
        <form [formGroup]="forgotForm" (ngSubmit)="onSubmit()" class="space-y-5">
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
              />
            </span>
          </div>

          @if (errorMessage()) {
            <p-message severity="error" [text]="errorMessage()" styleClass="w-full"></p-message>
          }

          <button 
            pButton 
            type="submit" 
            label="Envoyer le lien"
            class="w-full py-3"
            [loading]="loading()"
            [disabled]="forgotForm.invalid"
          ></button>
        </form>
      } @else {
        <div class="text-center py-8 animate-scale-in">
          <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-success-100 flex items-center justify-center">
            <i class="pi pi-send text-4xl text-success-600"></i>
          </div>
          <h3 class="text-2xl font-bold text-secondary-900 mb-2">Email envoyé !</h3>
          <p class="text-secondary-500 mb-2">Vérifiez votre boîte de réception.</p>
          <p class="text-sm text-secondary-400">Si vous ne trouvez pas l'email, vérifiez vos spams.</p>
        </div>
      }

      <p class="text-center mt-8 text-sm text-secondary-600">
        <a routerLink="/auth/login" class="text-primary-600 hover:text-primary-700 font-semibold inline-flex items-center gap-2">
          <i class="pi pi-arrow-left text-xs"></i>
          Retour à la connexion
        </a>
      </p>
    </div>
  `,
  styles: []
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  emailSent = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotForm.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.forgotPassword(this.forgotForm.value).subscribe({
      next: () => {
        this.loading.set(false);
        this.emailSent.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Erreur lors de l'envoi');
      }
    });
  }
}

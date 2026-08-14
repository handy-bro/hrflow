import { Component, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    PasswordModule, ButtonModule, MessageModule
  ],
  template: `
    <div class="animate-slide-up">
      <div class="text-center mb-8">
        <div class="w-16 h-16 mx-auto mb-4 rounded-2xl bg-primary-100 flex items-center justify-center">
          <i class="pi pi-key text-2xl text-primary-600"></i>
        </div>
        <h2 class="text-3xl font-bold text-secondary-900 mb-2">Nouveau mot de passe</h2>
        <p class="text-secondary-500">Créez un nouveau mot de passe sécurisé.</p>
      </div>

      @if (!resetSuccess()) {
        <form [formGroup]="resetForm" (ngSubmit)="onSubmit()" class="space-y-5">
          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Nouveau mot de passe</label>
            <p-password 
              formControlName="newPassword"
              [toggleMask]="true"
              placeholder="Minimum 8 caractères"
              styleClass="w-full"
              inputStyleClass="w-full"
            />
            <div class="flex gap-1 mt-1">
              @for (i of [1,2,3,4,5]; track i) {
                <div class="h-1 flex-1 rounded-full" 
                     [class.bg-success-500]="passwordStrength() >= i"
                     [class.bg-secondary-200]="passwordStrength() < i"></div>
              }
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Confirmer le mot de passe</label>
            <p-password 
              formControlName="confirmPassword"
              [toggleMask]="true"
              [feedback]="false"
              placeholder="Confirmez votre mot de passe"
              styleClass="w-full"
              inputStyleClass="w-full"
            />
            @if (resetForm.hasError('mismatch') && resetForm.get('confirmPassword')?.touched) {
              <small class="text-danger-500 text-xs">Les mots de passe ne correspondent pas</small>
            }
          </div>

          @if (errorMessage()) {
            <p-message severity="error" [text]="errorMessage()" styleClass="w-full"></p-message>
          }

          <button 
            pButton 
            type="submit" 
            label="Réinitialiser le mot de passe"
            class="w-full py-3"
            [loading]="loading()"
            [disabled]="resetForm.invalid"
          ></button>
        </form>
      } @else {
        <div class="text-center py-8 animate-scale-in">
          <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-success-100 flex items-center justify-center">
            <i class="pi pi-check-circle text-4xl text-success-600"></i>
          </div>
          <h3 class="text-2xl font-bold text-secondary-900 mb-2">Mot de passe réinitialisé !</h3>
          <p class="text-secondary-500 mb-6">Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.</p>
          <button 
            pButton 
            routerLink="/auth/login"
            label="Se connecter"
            class="w-full py-3"
          ></button>
        </div>
      }
    </div>
  `,
  styles: []
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  resetSuccess = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  passwordStrength = signal(0);
  token = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });

    this.resetForm.get('newPassword')?.valueChanges.subscribe(value => {
      this.calculateStrength(value);
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.router.navigate(['/auth/forgot-password']);
      }
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  calculateStrength(password: string): void {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    if (password.length >= 12) strength++;
    this.passwordStrength.set(Math.min(strength, 5));
  }

  onSubmit(): void {
    if (this.resetForm.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const data = {
      token: this.token,
      newPassword: this.resetForm.value.newPassword,
      confirmPassword: this.resetForm.value.confirmPassword
    };

    this.authService.resetPassword(data).subscribe({
      next: () => {
        this.loading.set(false);
        this.resetSuccess.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Erreur lors de la réinitialisation');
      }
    });
  }
}

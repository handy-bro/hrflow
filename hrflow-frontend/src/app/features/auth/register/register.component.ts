import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { MessageModule } from 'primeng/message';
import { StepsModule } from 'primeng/steps';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    InputTextModule, PasswordModule, ButtonModule,
    DropdownModule, MessageModule, StepsModule
  ],
  template: `
    <div class="animate-slide-up">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-secondary-900 mb-2">Créer un compte</h2>
        <p class="text-secondary-500">Rejoignez HRFlow pour gérer votre entreprise.</p>
      </div>

      <!-- Steps -->
      <p-steps [model]="steps" [activeIndex]="activeStep()" [readonly]="true" styleClass="mb-6"></p-steps>

      @if (activeStep() === 0) {
        <!-- Step 1: Account Info -->
        <form [formGroup]="accountForm" class="space-y-5">
          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Adresse email</label>
            <span class="p-input-icon-left w-full">
              <i class="pi pi-envelope text-secondary-400"></i>
              <input pInputText type="email" formControlName="email" placeholder="exemple@entreprise.com" class="w-full" />
            </span>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Rôle</label>
            <p-dropdown 
              formControlName="role"
              [options]="roles"
              placeholder="Sélectionnez un rôle"
              styleClass="w-full"
            />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Mot de passe</label>
            <p-password 
              formControlName="password"
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
          </div>

          <div class="flex items-center gap-2">
            <input type="checkbox" id="terms" class="rounded border-secondary-300 text-primary-600 focus:ring-primary-500" />
            <label for="terms" class="text-sm text-secondary-600">
              J'accepte les <a href="#" class="text-primary-600 hover:underline">conditions d'utilisation</a>
            </label>
          </div>

          <button 
            pButton 
            type="button"
            label="Continuer"
            class="w-full py-3"
            (click)="nextStep()"
            [disabled]="accountForm.invalid"
          ></button>
        </form>
      }

      @if (activeStep() === 1) {
        <!-- Step 2: Company Info -->
        <form [formGroup]="companyForm" class="space-y-5">
          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Nom de l'entreprise</label>
            <span class="p-input-icon-left w-full">
              <i class="pi pi-building text-secondary-400"></i>
              <input pInputText formControlName="companyName" placeholder="Votre entreprise" class="w-full" />
            </span>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Secteur d'activité</label>
            <p-dropdown 
              formControlName="sector"
              [options]="sectors"
              placeholder="Sélectionnez un secteur"
              styleClass="w-full"
            />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-secondary-700 block">Taille de l'entreprise</label>
            <p-dropdown 
              formControlName="size"
              [options]="sizes"
              placeholder="Nombre d'employés"
              styleClass="w-full"
            />
          </div>

          <div class="flex gap-3">
            <button 
              pButton 
              type="button"
              label="Retour"
              class="flex-1 py-3"
              severity="secondary"
              (click)="prevStep()"
            ></button>
            <button 
              pButton 
              type="button"
              label="S'inscrire"
              class="flex-1 py-3"
              (click)="onSubmit()"
              [loading]="authService.isLoading()"
              [disabled]="companyForm.invalid"
            ></button>
          </div>
        </form>
      }

      @if (registrationSuccess()) {
        <div class="text-center py-8 animate-scale-in">
          <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-success-100 flex items-center justify-center">
            <i class="pi pi-check-circle text-4xl text-success-600"></i>
          </div>
          <h3 class="text-2xl font-bold text-secondary-900 mb-2">Inscription réussie !</h3>
          <p class="text-secondary-500 mb-6">Un email de vérification a été envoyé à votre adresse.</p>
          <button 
            pButton 
            routerLink="/auth/login"
            label="Se connecter"
            class="w-full py-3"
          ></button>
        </div>
      }

      @if (errorMessage()) {
        <p-message severity="error" [text]="errorMessage()" styleClass="w-full mt-4"></p-message>
      }

      <p class="text-center mt-6 text-sm text-secondary-600">
        Déjà un compte ? 
        <a routerLink="/auth/login" class="text-primary-600 hover:text-primary-700 font-semibold">
          Se connecter
        </a>
      </p>
    </div>
  `,
  styles: []
})
export class RegisterComponent {
  activeStep = signal(0);
  registrationSuccess = signal(false);
  errorMessage = signal<string | null>(null);
  passwordStrength = signal(0);

  accountForm: FormGroup;
  companyForm: FormGroup;

  steps = [
    { label: 'Compte' },
    { label: 'Entreprise' }
  ];

  roles = [
    { label: 'Administrateur', value: 'ADMIN' },
    { label: 'Responsable RH', value: 'HR_MANAGER' },
    { label: 'Manager', value: 'MANAGER' },
    { label: 'Employé', value: 'EMPLOYEE' }
  ];

  sectors = [
    { label: 'Technologie', value: 'tech' },
    { label: 'Finance', value: 'finance' },
    { label: 'Santé', value: 'health' },
    { label: 'Éducation', value: 'education' },
    { label: 'Commerce', value: 'commerce' },
    { label: 'Industrie', value: 'industry' },
    { label: 'Autre', value: 'other' }
  ];

  sizes = [
    { label: '1-10 employés', value: 'micro' },
    { label: '11-50 employés', value: 'small' },
    { label: '51-200 employés', value: 'medium' },
    { label: '201-500 employés', value: 'large' },
    { label: '500+ employés', value: 'enterprise' }
  ];

  constructor(
    private fb: FormBuilder,
    public authService: AuthService
  ) {
    this.accountForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      role: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    });

    this.companyForm = this.fb.group({
      companyName: ['', Validators.required],
      sector: ['', Validators.required],
      size: ['', Validators.required]
    });

    this.accountForm.get('password')?.valueChanges.subscribe(value => {
      this.calculatePasswordStrength(value);
    });
  }

  calculatePasswordStrength(password: string): void {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;
    if (password.length >= 12) strength++;
    this.passwordStrength.set(Math.min(strength, 5));
  }

  nextStep(): void {
    if (this.accountForm.valid) {
      this.activeStep.update(v => v + 1);
    }
  }

  prevStep(): void {
    this.activeStep.update(v => v - 1);
  }

  onSubmit(): void {
    if (this.companyForm.invalid) return;

    const data = {
      email: this.accountForm.value.email,
      password: this.accountForm.value.password,
      role: this.accountForm.value.role
    };

    this.authService.register(data).subscribe({
      next: () => {
        this.registrationSuccess.set(true);
      },
      error: (err) => {
        this.errorMessage.set(err.message || 'Erreur lors de l'inscription');
      }
    });
  }
}

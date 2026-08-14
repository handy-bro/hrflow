import { Component, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule, ProgressSpinnerModule],
  template: `
    <div class="text-center py-12 animate-fade-in">
      @if (verifying()) {
        <div class="space-y-4">
          <p-progressSpinner 
            styleClass="w-16 h-16"
            strokeWidth="4"
            animationDuration="1s"
          ></p-progressSpinner>
          <p class="text-secondary-500">Vérification de votre email...</p>
        </div>
      } @else if (verified()) {
        <div class="animate-scale-in">
          <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-success-100 flex items-center justify-center">
            <i class="pi pi-check-circle text-4xl text-success-600"></i>
          </div>
          <h2 class="text-3xl font-bold text-secondary-900 mb-2">Email vérifié !</h2>
          <p class="text-secondary-500 mb-8">Votre compte est maintenant actif.</p>
          <button 
            pButton 
            routerLink="/auth/login"
            label="Se connecter"
            class="w-full py-3"
          ></button>
        </div>
      } @else {
        <div class="animate-scale-in">
          <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-danger-100 flex items-center justify-center">
            <i class="pi pi-times-circle text-4xl text-danger-600"></i>
          </div>
          <h2 class="text-3xl font-bold text-secondary-900 mb-2">Vérification échouée</h2>
          <p class="text-secondary-500 mb-4">{{ errorMessage() }}</p>
          <button 
            pButton 
            routerLink="/auth/login"
            label="Retour à la connexion"
            class="w-full py-3"
            severity="secondary"
          ></button>
        </div>
      }
    </div>
  `,
  styles: []
})
export class VerifyEmailComponent implements OnInit {
  verifying = signal(true);
  verified = signal(false);
  errorMessage = signal('Le lien de vérification est invalide ou a expiré.');

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.verifyToken(token);
      } else {
        this.verifying.set(false);
      }
    });
  }

  verifyToken(token: string): void {
    this.authService.verifyEmail(token).subscribe({
      next: () => {
        this.verifying.set(false);
        this.verified.set(true);
      },
      error: (err) => {
        this.verifying.set(false);
        this.errorMessage.set(err.message || 'Le lien de vérification est invalide ou a expiré.');
      }
    });
  }
}

import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <div class="min-h-screen flex">
      <!-- Left Side - Visual -->
      <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden">
        <!-- Background with gradient overlay -->
        <div class="absolute inset-0 bg-gradient-to-br from-primary-600 via-primary-700 to-primary-900"></div>

        <!-- Decorative elements -->
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-20 left-20 w-72 h-72 bg-white rounded-full blur-3xl"></div>
          <div class="absolute bottom-20 right-20 w-96 h-96 bg-primary-400 rounded-full blur-3xl"></div>
          <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-secondary-400 rounded-full blur-3xl"></div>
        </div>

        <!-- Grid pattern -->
        <div class="absolute inset-0 opacity-5" 
             style="background-image: radial-gradient(circle, #fff 1px, transparent 1px); background-size: 30px 30px;">
        </div>

        <!-- Content -->
        <div class="relative z-10 flex flex-col justify-between p-12 text-white">
          <div>
            <div class="flex items-center gap-3 mb-8">
              <div class="w-12 h-12 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
                <i class="pi pi-users text-2xl"></i>
              </div>
              <span class="text-2xl font-bold tracking-tight">HRFlow</span>
            </div>
          </div>

          <div class="space-y-8">
            <div class="space-y-4">
              <h1 class="text-4xl font-bold leading-tight">
                Gérez vos ressources humaines<br/>avec efficacité
              </h1>
              <p class="text-lg text-primary-100 max-w-md leading-relaxed">
                La solution complète pour la gestion des employés, congés, présences et fiches de paie.
              </p>
            </div>

            <!-- Features list -->
            <div class="space-y-4">
              @for (feature of features; track feature.text) {
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-white/20 backdrop-blur-sm flex items-center justify-center">
                    <i [class]="feature.icon + ' text-sm'"></i>
                  </div>
                  <span class="text-sm font-medium">{{ feature.text }}</span>
                </div>
              }
            </div>
          </div>

          <div class="text-sm text-primary-200">
            © 2026 HRFlow. Tous droits réservés.
          </div>
        </div>
      </div>

      <!-- Right Side - Form -->
      <div class="flex-1 flex items-center justify-center p-6 bg-secondary-50">
        <div class="w-full max-w-md">
          <!-- Mobile logo -->
          <div class="lg:hidden flex items-center justify-center gap-3 mb-8">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center shadow-lg shadow-primary-500/30">
              <i class="pi pi-users text-white text-xl"></i>
            </div>
            <span class="text-2xl font-bold text-secondary-800 tracking-tight">HRFlow</span>
          </div>

          <router-outlet />
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class AuthLayoutComponent {
  features = [
    { icon: 'pi pi-users', text: 'Gestion complète des employés' },
    { icon: 'pi pi-calendar', text: 'Suivi des congés et absences' },
    { icon: 'pi pi-chart-line', text: 'Tableaux de bord analytiques' },
    { icon: 'pi pi-shield', text: 'Sécurité et conformité' }
  ];
}

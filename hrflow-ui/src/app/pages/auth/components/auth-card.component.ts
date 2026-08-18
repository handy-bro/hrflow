import { Component, computed, input } from '@angular/core';
import { AppFloatingConfigurator } from '@/app/layout/component/app.floatingconfigurator';
import { ToastModule } from 'primeng/toast';

@Component({
    selector: 'app-auth-card',
    standalone: true,
    imports: [AppFloatingConfigurator, ToastModule],
    templateUrl: './auth-card.component.html',
    styleUrl: './auth-card.component.scss',
    host: {
        '[style.--auth-accent]': 'accentVar()'
    }
})
export class AuthCardComponent {
    readonly title = input.required<string>();
    readonly subtitle = input<string>('');
    readonly accent = input<'primary' | 'warn' | 'danger'>('primary');

    protected readonly accentVar = computed(() => `var(--${this.accent()}-color)`);
}

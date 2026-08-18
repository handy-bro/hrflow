import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [CommonModule, AppMenuitem, RouterModule],
    template: `<ul class="layout-menu">
        @for (item of model; track item.label) {
            @if (!item.separator) {
                <li app-menuitem [item]="item" [root]="true"></li>
            } @else {
                <li class="menu-separator"></li>
            }
        }
    </ul> `,
})
export class AppMenu {
    private readonly authService = inject(AuthService);

    model: MenuItem[] = [];

    ngOnInit() {
        const isAdmin = this.authService.hasRole('ADMIN');
        const isManager = this.authService.hasRole('MANAGER');

        this.model = [
            {
                label: 'Home',
                items: [{ label: 'Dashboard', icon: 'pi pi-fw pi-home', routerLink: ['/'] }]
            },
            {
                label: 'HR Management',
                items: [
                    { label: 'Employees', icon: 'pi pi-fw pi-users', routerLink: ['/employees'] },
                    { label: 'Departments', icon: 'pi pi-fw pi-building', routerLink: ['/departments'] },
                    ...(isAdmin || isManager ? [{ label: 'Payslips', icon: 'pi pi-fw pi-money-bill', routerLink: ['/payslips'] }] : []),
                    { label: 'Leaves', icon: 'pi pi-fw pi-calendar-times', routerLink: ['/leaves'] },
                    { label: 'Attendance', icon: 'pi pi-fw pi-clock', routerLink: ['/attendance'] }
                ]
            },
            {
                label: 'Account',
                items: [
                    { label: 'Profile', icon: 'pi pi-fw pi-user', routerLink: ['/profile'] },
                    { label: 'Logout', icon: 'pi pi-fw pi-sign-out', command: () => this.authService.logout() }
                ]
            }
        ];
    }
}

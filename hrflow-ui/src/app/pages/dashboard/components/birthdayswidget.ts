import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaginatorModule } from 'primeng/paginator';
import { BadgeModule } from 'primeng/badge';
import { DashboardService } from '@/app/core/services/dashboard.service';
import { BirthdayAlertEntry } from '@/app/core/dto/dashboard.dto';

@Component({
    standalone: true,
    selector: 'app-birthdays-widget',
    imports: [CommonModule, PaginatorModule, BadgeModule],
    template: `
        @if (birthdays(); as items) {
            <div class="card">
                <div class="flex items-center justify-between mb-6">
                    <div class="font-semibold text-xl">Birthdays Today</div>
                    <p-badge [value]="items.length.toString()" severity="info" />
                </div>

                <ul class="p-0 m-0 list-none">
                    @for (item of pagedItems(); track item.employeeId) {
                        <li class="flex items-center py-2 border-b border-surface last:border-b-0">
                            <div class="w-12 h-12 flex items-center justify-center bg-blue-100 dark:bg-blue-400/10 rounded-full mr-4 shrink-0">
                                <i class="pi pi-gift text-xl! text-blue-500"></i>
                            </div>
                            <span class="text-surface-900 dark:text-surface-0 leading-normal">{{ item.fullName }} — birthday today 🎉</span>
                        </li>
                    } @empty {
                        <li class="text-muted-color">No birthdays today.</li>
                    }
                </ul>

                @if (items.length > pageSize) {
                    <p-paginator [first]="first()" [rows]="pageSize" [totalRecords]="items.length" (onPageChange)="onPageChange($event)" styleClass="mt-2" />
                }
            </div>
        }
    `
})
export class BirthdaysWidget {
    private readonly dashboardService = inject(DashboardService);

    readonly birthdays = signal<BirthdayAlertEntry[]>([]);
    readonly pageSize = 5;
    readonly first = signal(0);

    readonly pagedItems = computed(() => this.birthdays().slice(this.first(), this.first() + this.pageSize));

    onPageChange(event: { first?: number }): void {
        this.first.set(event.first ?? 0);
    }

    ngOnInit(): void {
        this.dashboardService.getAlerts().subscribe((data) => this.birthdays.set(data.birthdaysToday));
    }
}

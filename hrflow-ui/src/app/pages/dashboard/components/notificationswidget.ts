import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaginatorModule } from 'primeng/paginator';
import { BadgeModule } from 'primeng/badge';
import { DashboardService } from '@/app/core/services/dashboard.service';
import { DashboardAlertsResponse } from '@/app/core/dto/dashboard.dto';

@Component({
    standalone: true,
    selector: 'app-notifications-widget',
    imports: [CommonModule, PaginatorModule, BadgeModule],
    template: `
        @if (alerts(); as data) {
            <div class="card">
                <div class="font-semibold text-xl mb-6">Alerts</div>

                <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
                    <!-- Contracts -->
                    <div class="p-4 rounded-border border border-surface bg-surface-0 dark:bg-surface-900">
                        <div class="flex items-center justify-between mb-4">
                            <span class="text-muted-color font-medium">CONTRACTS EXPIRING SOON</span>
                            <p-badge [value]="data.contractsExpiringSoon.length.toString()" severity="warn" />
                        </div>
                        <ul class="p-0 m-0 list-none">
                            @for (item of pagedContracts(); track item.employeeId) {
                                <li class="flex items-center py-2 border-b border-surface last:border-b-0">
                                    <div class="w-12 h-12 flex items-center justify-center bg-orange-100 dark:bg-orange-400/10 rounded-full mr-4 shrink-0">
                                        <i class="pi pi-file-edit text-xl! text-orange-500"></i>
                                    </div>
                                    <span class="text-surface-900 dark:text-surface-0 leading-normal">{{ item.fullName }} — {{ item.daysRemaining }} days remaining</span>
                                </li>
                            } @empty {
                                <li class="text-muted-color">No contracts expiring soon.</li>
                            }
                        </ul>
                        @if (data.contractsExpiringSoon.length > pageSize) {
                            <p-paginator [first]="contractsFirst()" [rows]="pageSize" [totalRecords]="data.contractsExpiringSoon.length" (onPageChange)="onContractsPageChange($event)" styleClass="mt-2" />
                        }
                    </div>

                    <!-- Pending Leaves -->
                    <div class="p-4 rounded-border border border-surface bg-surface-0 dark:bg-surface-900">
                        <div class="flex items-center justify-between mb-4">
                            <span class="text-muted-color font-medium">PENDING LEAVES</span>
                            <p-badge [value]="data.pendingLeaves.length.toString()" severity="danger" />
                        </div>
                        <ul class="p-0 m-0 list-none">
                            @for (item of pagedLeaves(); track item.leaveRequestId) {
                                <li class="flex items-center py-2 border-b border-surface last:border-b-0">
                                    <div class="w-12 h-12 flex items-center justify-center rounded-full mr-4 shrink-0" [class.bg-red-100]="item.urgent" [class.dark:bg-red-400/10]="item.urgent" [class.bg-green-100]="!item.urgent" [class.dark:bg-green-400/10]="!item.urgent">
                                        <i class="text-xl!" [class.pi]="true" [class.pi-exclamation-circle]="item.urgent" [class.text-red-500]="item.urgent" [class.pi-calendar]="!item.urgent" [class.text-green-500]="!item.urgent"></i>
                                    </div>
                                    <span class="text-surface-900 dark:text-surface-0 leading-normal">{{ item.employeeName }} — {{ item.leaveType }} ({{ item.daysWaiting }} days waiting)</span>
                                </li>
                            } @empty {
                                <li class="text-muted-color">No pending leaves.</li>
                            }
                        </ul>
                        @if (data.pendingLeaves.length > pageSize) {
                            <p-paginator [first]="leavesFirst()" [rows]="pageSize" [totalRecords]="data.pendingLeaves.length" (onPageChange)="onLeavesPageChange($event)" styleClass="mt-2" />
                        }
                    </div>
                </div>
            </div>
        }
    `
})
export class NotificationsWidget {
    private readonly dashboardService = inject(DashboardService);

    readonly alerts = signal<DashboardAlertsResponse | null>(null);
    readonly pageSize = 5;

    readonly contractsFirst = signal(0);
    readonly leavesFirst = signal(0);

    readonly pagedContracts = computed(() => {
        const data = this.alerts();
        if (!data) return [];
        return data.contractsExpiringSoon.slice(this.contractsFirst(), this.contractsFirst() + this.pageSize);
    });

    readonly pagedLeaves = computed(() => {
        const data = this.alerts();
        if (!data) return [];
        return data.pendingLeaves.slice(this.leavesFirst(), this.leavesFirst() + this.pageSize);
    });

    onContractsPageChange(event: { first?: number }): void {
        this.contractsFirst.set(event.first ?? 0);
    }

    onLeavesPageChange(event: { first?: number }): void {
        this.leavesFirst.set(event.first ?? 0);
    }

    ngOnInit(): void {
        this.dashboardService.getAlerts().subscribe((data) => this.alerts.set(data));
    }
}

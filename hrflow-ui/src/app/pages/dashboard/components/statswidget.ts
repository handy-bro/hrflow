import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '@/app/core/services/dashboard.service';
import { DashboardSummaryResponse } from '@/app/core/dto/dashboard.dto';

@Component({
    standalone: true,
    selector: 'app-stats-widget',
    imports: [CommonModule],
    template: `
        @if (summary(); as data) {
            <div class="col-span-12 lg:col-span-6 xl:col-span-3">
                <div class="card mb-0">
                    <div class="flex justify-between mb-4">
                        <div>
                            <span class="block text-muted-color font-medium mb-4">Active Employees</span>
                            <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ data.totalActiveEmployees }}</div>
                        </div>
                        <div class="flex items-center justify-center bg-blue-100 dark:bg-blue-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                            <i class="pi pi-users text-blue-500 text-xl!"></i>
                        </div>
                    </div>
                    <span class="text-primary font-medium">{{ data.newEmployeesThisMonth }} new </span>
                    <span class="text-muted-color">this month</span>
                </div>
            </div>

            <div class="col-span-12 lg:col-span-6 xl:col-span-3">
                <div class="card mb-0">
                    <div class="flex justify-between mb-4">
                        <div>
                            <span class="block text-muted-color font-medium mb-4">Monthly Growth</span>
                            <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ data.monthlyGrowthPercent | number: '1.1-1' }}%</div>
                        </div>
                        <div class="flex items-center justify-center bg-orange-100 dark:bg-orange-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                            <i class="pi pi-chart-line text-orange-500 text-xl!"></i>
                        </div>
                    </div>
                    <span class="text-primary font-medium">{{ data.employeesLastMonth }} </span>
                    <span class="text-muted-color">last month</span>
                </div>
            </div>

            <div class="col-span-12 lg:col-span-6 xl:col-span-3">
                <div class="card mb-0">
                    <div class="flex justify-between mb-4">
                        <div>
                            <span class="block text-muted-color font-medium mb-4">Pending Leaves</span>
                            <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ data.pendingLeaveRequests }}</div>
                        </div>
                        <div class="flex items-center justify-center bg-cyan-100 dark:bg-cyan-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                            <i class="pi pi-calendar-times text-cyan-500 text-xl!"></i>
                        </div>
                    </div>
                    <span class="text-primary font-medium">{{ data.urgentLeaveRequests }} </span>
                    <span class="text-muted-color">urgent</span>
                </div>
            </div>

            <div class="col-span-12 lg:col-span-6 xl:col-span-3">
                <div class="card mb-0">
                    <div class="flex justify-between mb-4">
                        <div>
                            <span class="block text-muted-color font-medium mb-4">New Hires</span>
                            <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ data.newEmployeesThisMonth }}</div>
                        </div>
                        <div class="flex items-center justify-center bg-purple-100 dark:bg-purple-400/10 rounded-border" style="width: 2.5rem; height: 2.5rem">
                            <i class="pi pi-user-plus text-purple-500 text-xl!"></i>
                        </div>
                    </div>
                    <span class="text-primary font-medium">this month</span>
                </div>
            </div>
        }
    `
})
export class StatsWidget {
    private readonly dashboardService = inject(DashboardService);

    readonly summary = signal<DashboardSummaryResponse | null>(null);

    ngOnInit(): void {
        this.dashboardService.getSummary().subscribe((data) => this.summary.set(data));
    }
}

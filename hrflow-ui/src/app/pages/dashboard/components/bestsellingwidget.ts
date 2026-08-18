import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '@/app/core/services/dashboard.service';
import { DepartmentDistributionEntry } from '@/app/core/dto/dashboard.dto';

@Component({
    standalone: true,
    selector: 'app-best-selling-widget',
    imports: [CommonModule],
    template: `
        <div class="card">
            <div class="font-semibold text-xl mb-6">Department Distribution</div>
            <ul class="list-none p-0 m-0">
                @for (dept of departments(); track dept.departmentId) {
                    <li class="flex flex-col md:flex-row md:items-center md:justify-between mb-6">
                        <div>
                            <span class="text-surface-900 dark:text-surface-0 font-medium mr-2 mb-1 md:mb-0">{{ dept.departmentName }}</span>
                        </div>
                        <div class="mt-2 md:mt-0 flex items-center">
                            <div class="bg-surface-300 dark:bg-surface-500 rounded-border overflow-hidden w-40 lg:w-24" style="height: 8px">
                                <div class="bg-primary h-full" [style.width.%]="distributionPercent(dept.employeeCount)"></div>
                            </div>
                            <span class="text-primary ml-4 font-medium">{{ dept.employeeCount }}</span>
                        </div>
                    </li>
                }
            </ul>
        </div>
    `
})
export class BestSellingWidget {
    private readonly dashboardService = inject(DashboardService);

    readonly departments = signal<DepartmentDistributionEntry[]>([]);
    readonly totalEmployees = signal<number>(0);

    ngOnInit(): void {
        this.dashboardService.getDepartmentDistribution().subscribe((data) => {
            this.departments.set(data);
            this.totalEmployees.set(data.reduce((sum, d) => sum + d.employeeCount, 0));
        });
    }

    distributionPercent(count: number): number {
        const total = this.totalEmployees();
        return total > 0 ? Math.round((count / total) * 100) : 0;
    }
}

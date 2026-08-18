import { Component, inject, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { DashboardService } from '@/app/core/services/dashboard.service';
import { NewEmployeeEntry } from '@/app/core/dto/dashboard.dto';

@Component({
    standalone: true,
    selector: 'app-recent-sales-widget',
    imports: [CommonModule, TableModule, ButtonModule],
    template: `<div class="card mb-8!">
        <div class="font-semibold text-xl mb-4">New Employees</div>
        <p-table [value]="newEmployees()" [paginator]="true" [rows]="5" responsiveLayout="scroll">
            <ng-template #header>
                <tr>
                    <th>Name</th>
                    <th>Position</th>
                    <th>Department</th>
                    <th>Hire Date</th>
                </tr>
            </ng-template>
            <ng-template #body let-employee>
                <tr>
                    <td>{{ employee.fullName }}</td>
                    <td>{{ employee.position }}</td>
                    <td>{{ employee.departmentName }}</td>
                    <td>{{ employee.hireDate | date: 'mediumDate' }}</td>
                </tr>
            </ng-template>
        </p-table>
    </div>`
})
export class RecentSalesWidget {
    private readonly dashboardService = inject(DashboardService);

    readonly newEmployees = signal<NewEmployeeEntry[]>([]);

    ngOnInit(): void {
        this.dashboardService.getNewEmployees().subscribe((data) => this.newEmployees.set(data));
    }
}

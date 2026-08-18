import { afterNextRender, Component, effect, inject, signal } from '@angular/core';
import { ChartModule } from 'primeng/chart';
import { LayoutService } from '@/app/layout/service/layout.service';
import { DashboardService } from '@/app/core/services/dashboard.service';
import { WorkforceTrendEntry } from '@/app/core/dto/dashboard.dto';

@Component({
    standalone: true,
    selector: 'app-revenue-stream-widget',
    imports: [ChartModule],
    template: `<div class="card mb-8!">
        <div class="font-semibold text-xl mb-4">Workforce Trend (12 months)</div>
        <p-chart type="line" [data]="chartData()" [options]="chartOptions()" class="h-100" />
    </div>`
})
export class RevenueStreamWidget {
    private readonly layoutService = inject(LayoutService);
    private readonly dashboardService = inject(DashboardService);

    readonly chartData = signal<any>(null);
    readonly chartOptions = signal<any>(null);
    readonly trend = signal<WorkforceTrendEntry[]>([]);

    constructor() {
        afterNextRender(() => {
            setTimeout(() => this.initChart(), 150);
        });

        effect(() => {
            this.layoutService.layoutConfig().darkTheme;
            setTimeout(() => this.initChart(), 150);
        });
    }

    ngOnInit(): void {
        this.dashboardService.getWorkforceTrend().subscribe((data) => {
            this.trend.set(data);
            this.initChart();
        });
    }

    initChart() {
        const documentStyle = getComputedStyle(document.documentElement);
        const textColor = documentStyle.getPropertyValue('--text-color');
        const borderColor = documentStyle.getPropertyValue('--surface-border');
        const textMutedColor = documentStyle.getPropertyValue('--text-color-secondary');

        const labels = this.trend().map((entry) => `${entry.month}/${entry.year}`);
        const values = this.trend().map((entry) => entry.activeEmployees);

        this.chartData.set({
            labels,
            datasets: [
                {
                    type: 'line',
                    label: 'Active Employees',
                    borderColor: documentStyle.getPropertyValue('--p-primary-400'),
                    backgroundColor: documentStyle.getPropertyValue('--p-primary-100'),
                    data: values,
                    fill: true,
                    tension: 0.4
                }
            ]
        });

        this.chartOptions.set({
            maintainAspectRatio: false,
            aspectRatio: 0.8,
            plugins: {
                legend: {
                    labels: {
                        color: textColor
                    }
                }
            },
            scales: {
                x: {
                    ticks: {
                        color: textMutedColor
                    },
                    grid: {
                        color: 'transparent',
                        borderColor: 'transparent'
                    }
                },
                y: {
                    ticks: {
                        color: textMutedColor
                    },
                    grid: {
                        color: borderColor,
                        borderColor: 'transparent',
                        drawTicks: false
                    }
                }
            }
        });
    }
}

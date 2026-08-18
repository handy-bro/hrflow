import { Component } from '@angular/core';
import { NotificationsWidget } from './components/notificationswidget';
import { StatsWidget } from './components/statswidget';
import { RecentSalesWidget } from './components/recentsaleswidget';
import { BestSellingWidget } from './components/bestsellingwidget';
import { RevenueStreamWidget } from './components/revenuestreamwidget';
import { BirthdaysWidget } from './components/birthdayswidget';

@Component({
    selector: 'app-dashboard',
    imports: [StatsWidget, RecentSalesWidget, BestSellingWidget, RevenueStreamWidget, NotificationsWidget, BirthdaysWidget],
    template: `
        <div class="grid grid-cols-12 gap-8 items-start">
            <app-stats-widget class="contents" />

            <div class="col-span-12 xl:col-span-6 flex flex-col gap-8">
                <app-recent-sales-widget />
                <app-revenue-stream-widget />
            </div>

            <div class="col-span-12 xl:col-span-6 flex flex-col gap-8">
                <app-best-selling-widget />
                <app-birthdays-widget />
            </div>

            <div class="col-span-12">
                <app-notifications-widget />
            </div>
        </div>
    `
})
export class Dashboard {}

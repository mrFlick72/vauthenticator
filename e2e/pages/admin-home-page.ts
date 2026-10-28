import {expect, Locator, Page} from '@playwright/test';
import {ClientApplicationListPage} from './client-application-list-page';

export class AdminHomePage {
    static readonly PATH = '/secure/admin/index';

    readonly clientApplicationSection: Locator;

    constructor(private readonly page: Page) {
        this.clientApplicationSection = page.getByText('Client Application Management Section');
    }

    async goto() {
        await this.page.goto(AdminHomePage.PATH);
    }

    async openClientApplications() {
        await this.clientApplicationSection.click();
        await expect(this.page).toHaveURL(/\/client-applications\/list$/);
        return new ClientApplicationListPage(this.page);
    }
}

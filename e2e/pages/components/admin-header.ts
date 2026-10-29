import {expect, Locator, Page} from '@playwright/test';
import {AUTH_SERVER_URL, MANAGEMENT_UI_URL} from '../../playwright.config';

// the app bar AdminTemplate renders on every page
export class AdminHeader {
    readonly title: Locator;
    readonly homeLink: Locator;
    readonly logoutLink: Locator;

    constructor(private readonly page: Page) {
        const banner = page.getByRole('banner');
        this.title = banner.getByRole('heading');
        // both links wrap an IconButton labelled "menu", so they share that accessible name
        this.homeLink = banner.locator('a[href="/secure/admin/index"]');
        this.logoutLink = banner.locator('a[href="/logout"]');
    }

    // /logout ends the session at the auth-server (/connect/logout), which redirects back to the management ui
    async logout() {
        const endSession = this.page.waitForResponse(r => r.url().startsWith(`${AUTH_SERVER_URL}/connect/logout`));
        await this.logoutLink.click();
        await endSession;
        await this.page.waitForURL(url => url.href.startsWith(MANAGEMENT_UI_URL) && !url.pathname.startsWith('/logout'));
        await expect(this.page).not.toHaveURL(/\/logout/);
    }
}

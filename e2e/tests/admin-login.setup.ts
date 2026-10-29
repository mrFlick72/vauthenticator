import {expect, test as setup} from '@playwright/test';
import {ADMIN_STORAGE_STATE} from '../playwright.config';
import {AdminHomePage} from '../pages/admin-home-page';
import {LoginPage} from '../pages/login-page';
import {ADMIN_ACCOUNT} from '../support/accounts';

// storageState persists cookies and localStorage but not sessionStorage, where the
// management ui keeps its tokens: what is reused across tests is the auth-server
// session cookie, so each test still goes through /oauth2/authorize, without the login form
setup('admin login', async ({page}) => {
    const adminHome = new AdminHomePage(page);
    await adminHome.goto();

    await new LoginPage(page).login(ADMIN_ACCOUNT.email, ADMIN_ACCOUNT.password);

    await expect(page).toHaveURL(/\/secure\/admin\/index$/);
    await expect(adminHome.clientApplicationSection).toBeVisible();

    await page.context().storageState({path: ADMIN_STORAGE_STATE});
});

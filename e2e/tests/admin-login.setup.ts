import {expect, test as setup} from '@playwright/test';
import {ADMIN_STORAGE_STATE} from '../playwright.config';

// default admin account provisioned by the tenant-setup actuator endpoint
const ADMIN_USERNAME = 'admin@email.com';
const ADMIN_PASSWORD = 'secret!';

// storageState persists cookies and localStorage but not sessionStorage, where the
// management ui keeps its tokens: what is reused across tests is the auth-server
// session cookie, so each test still goes through /oauth2/authorize, without the login form
setup('admin login', async ({page}) => {
    await page.goto('/secure/admin/index');

    await page.locator('#username').fill(ADMIN_USERNAME);
    await page.locator('#password').fill(ADMIN_PASSWORD);
    await page.getByRole('button', {name: 'Login'}).click();

    await expect(page).toHaveURL(/\/secure\/admin\/index$/);
    await expect(page.getByText('Client Application Management Section')).toBeVisible();

    await page.context().storageState({path: ADMIN_STORAGE_STATE});
});

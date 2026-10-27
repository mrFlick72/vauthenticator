import {expect, test} from '@playwright/test';

test('create a new client application', async ({page}) => {
    const clientAppId = `e2e-${Date.now()}`;
    const clientAppName = `E2E client application ${clientAppId}`;
    const redirectUri = `https://${clientAppId}.example.com/callback`;

    await page.goto('/secure/admin/index');
    await page.getByText('Client Application Management Section').click();
    await expect(page).toHaveURL(/\/client-applications\/list$/);

    await page.getByRole('button', {name: 'New Client Application'}).click();
    await expect(page).toHaveURL(/\/client-applications\/save$/);

    // tab 1: credentials
    await page.locator('#clientAppId').fill(clientAppId);
    await page.locator('#secret').fill('secret');
    await page.locator('#clientAppName').fill(clientAppName);
    await page.getByRole('button', {name: 'Next Tab'}).click();

    // tab 2: permissions
    await page.locator('#scopes input').fill('openid');
    await page.keyboard.press('Enter');
    await page.getByLabel('authorization_code').check();
    await page.locator('#accessTokenValidity').fill('3600');
    await page.locator('#refreshTokenValidity').fill('3600');
    await page.getByRole('button', {name: 'Next Tab'}).click();

    // tab 3: uris
    await page.locator('#webServerRedirectUri').fill(redirectUri);
    await page.locator('#allowedOrigins').fill(`https://${clientAppId}.example.com`);
    await page.locator('#postLogoutRedirectUri').fill(`https://${clientAppId}.example.com`);
    await page.locator('#logoutUri').fill(`https://${clientAppId}.example.com/logout`);
    await page.getByRole('button', {name: 'Save Client Application'}).click();

    await expect(page).toHaveURL(/\/client-applications\/list$/);
    // StickyHeadTable body rows carry role="checkbox", so getByRole('row') does not match them
    const row = page.locator('tr').filter({hasText: clientAppId});
    await expect(row).toBeVisible();

    // reopen it to prove the values made it through the api and back
    // the "Edit Application" column (5th) holds a bare icon with no accessible name
    await row.locator('td').nth(4).locator('svg').click();
    await expect(page).toHaveURL(new RegExp(`/client-applications/edit/${clientAppId}$`));
    await expect(page.locator('#clientAppName')).toHaveValue(clientAppName);
    await page.getByRole('tab', {name: /Uri Section/}).click();
    await expect(page.locator('#webServerRedirectUri')).toHaveValue(redirectUri);
});

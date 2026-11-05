import {expect, test} from '@playwright/test';
import {MANAGEMENT_UI_URL} from '../playwright.config';
import {AdminHomePage} from '../pages/admin-home-page';
import {SessionExpiryDialogs} from '../pages/components/session-expiry-dialog';
import {LoginPage} from '../pages/login-page';
import {RolesPage} from '../pages/roles-page';
import {ADMIN_ACCOUNT, loginInFreshBrowser} from '../support/accounts';
import {failApiCall} from '../support/api-failures';
import {accessToken, expiredJwt, replaceAccessToken, trackAuthorizeNavigations} from '../support/session';

// a 401 from the api opens the "Access expired" dialog, whose renewal needs credentials only once the
// auth-server session has ended (management-ui/CONTEXT.md). Roles is only the stage: a page with one api call.
const ROLES_URL = /\/secure\/admin\/roles$/;

let roles: RolesPage;
let dialogs: SessionExpiryDialogs;

test.beforeEach(async ({page}) => {
    const home = new AdminHomePage(page);
    await home.goto();
    roles = await home.openRoles();
    dialogs = new SessionExpiryDialogs(page);
});

test('an expired access is renewed from the living session without credentials', async ({page}) => {
    await failApiCall(page, 'GET', '/api/roles', 401);
    await page.reload();

    await expect(dialogs.accessExpired).toBeVisible();
    await expect(dialogs.any).toHaveCount(1);
    await expect(roles.failureBehindDialog).toHaveText('Could not load the roles: your access has expired');

    await page.unrouteAll({behavior: 'ignoreErrors'});
    await dialogs.continueButton.click();

    // back on the same page through the authorize endpoint, with no login form on the way
    await expect(page).toHaveURL(ROLES_URL);
    await expect(roles.table.rowsPerPage).toBeVisible();
    await expect(dialogs.any).toBeHidden();
});

test('a malformed access token is renewed on page load without a dialog', async ({page}) => {
    await replaceAccessToken(page, 'not-a-token');
    const authorizations = trackAuthorizeNavigations(page);

    await page.reload();

    await expect.poll(() => authorizations.length).toBeGreaterThan(0);
    expect(authorizations[0]).toContain('prompt=none');
    await expect(page).toHaveURL(ROLES_URL);
    await expect(roles.table.rowsPerPage).toBeVisible();
    expect(await accessToken(page)).not.toBe('not-a-token');
});

test('an expired access token is renewed on page load without a dialog', async ({page}) => {
    const expired = expiredJwt();
    await replaceAccessToken(page, expired);
    const authorizations = trackAuthorizeNavigations(page);

    await page.reload();

    await expect.poll(() => authorizations.length).toBeGreaterThan(0);
    await expect(page).toHaveURL(ROLES_URL);
    await expect(roles.table.rowsPerPage).toBeVisible();
    expect(await accessToken(page)).not.toBe(expired);
});

// the scenarios below need a browser with no auth-server session: they never touch the shared admin session

test('a callback the session could not answer silently goes on to the login form', async ({browser}) => {
    const context = await browser.newContext({baseURL: MANAGEMENT_UI_URL, storageState: {cookies: [], origins: []}});
    try {
        const page = await context.newPage();

        await page.goto('/callback?error=login_required');

        await expect(new LoginPage(page).username).toBeVisible();
    } finally {
        await context.close();
    }
});

test('a rejected access whose session has ended leads to the login form', async ({browser}) => {
    const {context, page} = await loginInFreshBrowser(browser, ADMIN_ACCOUNT);
    try {
        const home = new AdminHomePage(page);
        await expect(home.clientApplicationSection).toBeVisible();
        const freshRoles = await home.openRoles();
        const freshDialogs = new SessionExpiryDialogs(page);

        // without its cookie the auth-server session is gone, while the ui still holds its tokens
        await context.clearCookies();
        await failApiCall(page, 'GET', '/api/roles', 401);
        await page.reload();

        // the OP session iframe may notice the ended session first and open "Session expired" instead:
        // either dialog leads to the login form
        await expect(freshDialogs.any).toBeVisible();
        await expect(freshRoles.failureBehindDialog).toHaveText('Could not load the roles: your access has expired');
        await page.unrouteAll({behavior: 'ignoreErrors'});

        await freshDialogs.proceedButton.click();

        await expect(new LoginPage(page).username).toBeVisible();
    } finally {
        await context.close();
    }
});

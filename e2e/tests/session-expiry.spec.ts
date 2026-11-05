import {expect, test} from '@playwright/test';
import {MANAGEMENT_UI_URL} from '../playwright.config';
import {AdminHomePage} from '../pages/admin-home-page';
import {LoginPage} from '../pages/login-page';
import {RolesPage} from '../pages/roles-page';
import {ADMIN_ACCOUNT, loginInFreshBrowser} from '../support/accounts';
import {failApiCall} from '../support/api-failures';
import {accessToken, expiredJwt, replaceAccessToken, trackAuthorizeNavigations} from '../support/session';

// every admin page has the auth-server validate its access token on load, and renews a rejected one: silently
// while the session lives, through the login form once it has ended (management-ui/CONTEXT.md). Roles is only
// the stage: a page with one api call.
const ROLES_URL = /\/secure\/admin\/roles$/;

let roles: RolesPage;

test.beforeEach(async ({page}) => {
    const home = new AdminHomePage(page);
    await home.goto();
    roles = await home.openRoles();
});

test('a malformed access token is renewed on page load', async ({page}) => {
    await replaceAccessToken(page, 'not-a-token');
    const authorizations = trackAuthorizeNavigations(page);

    await page.reload();

    await expect.poll(() => authorizations.length).toBeGreaterThan(0);
    expect(authorizations[0]).toContain('prompt=none');
    await expect(page).toHaveURL(ROLES_URL);
    await expect(roles.table.rowsPerPage).toBeVisible();
    expect(await accessToken(page)).not.toBe('not-a-token');
});

test('an expired access token is renewed on page load', async ({page}) => {
    const expired = expiredJwt();
    await replaceAccessToken(page, expired);
    const authorizations = trackAuthorizeNavigations(page);

    await page.reload();

    await expect.poll(() => authorizations.length).toBeGreaterThan(0);
    await expect(page).toHaveURL(ROLES_URL);
    await expect(roles.table.rowsPerPage).toBeVisible();
    expect(await accessToken(page)).not.toBe(expired);
});

test('a token check the auth-server cannot answer does not redirect', async ({page}) => {
    await failApiCall(page, 'GET', '/userinfo', 500);
    const authorizations = trackAuthorizeNavigations(page);
    // the GET, not its preflight: the redirect decision follows this answer
    const tokenCheck = page.waitForResponse(r => r.request().method() === 'GET' && new URL(r.url()).pathname === '/userinfo');

    await page.reload();
    await tokenCheck;

    await expect(roles.table.rowsPerPage).toBeVisible();
    await expect(page).toHaveURL(ROLES_URL);
    expect(authorizations).toHaveLength(0);
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

test('a rejected access token whose session has ended leads to the login form', async ({browser}) => {
    const {context, page} = await loginInFreshBrowser(browser, ADMIN_ACCOUNT);
    try {
        const home = new AdminHomePage(page);
        await expect(home.clientApplicationSection).toBeVisible();
        await home.openRoles();

        // without its cookie the auth-server session is gone, and the token is rejected on the next load
        await context.clearCookies();
        await replaceAccessToken(page, 'not-a-token');
        await page.reload();

        await expect(new LoginPage(page).username).toBeVisible();
    } finally {
        await context.close();
    }
});

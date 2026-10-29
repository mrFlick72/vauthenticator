import {APIRequestContext, Browser, expect} from '@playwright/test';
import {MANAGEMENT_UI_URL} from '../playwright.config';
import {AdminHomePage} from '../pages/admin-home-page';
import {LoginPage} from '../pages/login-page';
import {AdminApi} from './admin-api';
import {uniqueId} from './unique';

export type TestAccount = { email: string; password: string };

// default admin account provisioned by the tenant-setup actuator endpoint
export const ADMIN_ACCOUNT: TestAccount = {email: 'admin@email.com', password: 'secret!'};

// a domain of its own lets a scenario target exactly its accounts with an account pattern
export function anAccount(domain = `${uniqueId()}.e2e.example.com`): TestAccount {
    return {email: `${uniqueId()}@${domain}`, password: 'Secret!12'};
}

// sign up leaves the account locked and disabled until its email is verified: unlock it through the api
export async function signUpActiveAccount(request: APIRequestContext, account: TestAccount = anAccount()) {
    const adminApi = await AdminApi.create(request);
    await adminApi.signUp(account.email, account.password);
    const signedUp = await adminApi.findAccount(account.email);
    await adminApi.saveAccount({...signedUp, enabled: true, accountLocked: false});
    return account;
}

export const LOGIN_FAILED_URL = /\/login\?error$/;

// logs in through the management ui in a browser with no session of its own, so neither the shared admin
// session nor any other test is touched; the caller asserts where the login ended up and closes the context
export async function loginInFreshBrowser(browser: Browser, account: TestAccount) {
    const context = await browser.newContext({baseURL: MANAGEMENT_UI_URL, storageState: {cookies: [], origins: []}});
    const page = await context.newPage();
    await new AdminHomePage(page).goto();
    await new LoginPage(page).login(account.email, account.password);
    return {context, page};
}

export async function expectLoginSucceeds(browser: Browser, account: TestAccount) {
    const {context, page} = await loginInFreshBrowser(browser, account);
    await expect(page).toHaveURL(new RegExp(`^${MANAGEMENT_UI_URL}/`));
    await context.close();
}

export async function expectLoginFails(browser: Browser, account: TestAccount) {
    const {context, page} = await loginInFreshBrowser(browser, account);
    await expect(page).toHaveURL(LOGIN_FAILED_URL);
    await context.close();
}

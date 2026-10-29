import {expect, test} from '@playwright/test';
import {AccountSearchPage} from '../pages/account-search-page';
import {AdminHomePage} from '../pages/admin-home-page';
import {AdminApi} from '../support/admin-api';
import {
    anAccount,
    expectLoginFails,
    expectLoginSucceeds,
    loginInFreshBrowser,
    signUpActiveAccount
} from '../support/accounts';

// every scenario edits an account of its own, never the admin: locking or disabling the admin would end
// the run. Effects are proven by logging in as that account from a separate browser.

let accounts: AccountSearchPage;

test.beforeEach(async ({page}) => {
    const home = new AdminHomePage(page);
    await home.goto();
    accounts = await home.openAccounts();
});

test('find an account and see its current state', async ({request}) => {
    const account = await signUpActiveAccount(request);

    const editor = await accounts.find(account.email);

    await expect(editor.email).toHaveValue(account.email);
    await expect(editor.enabled).toBeChecked();
    await expect(editor.accountLocked).not.toBeChecked();
    await expect(editor.mandatoryAction.container).toContainText('NO_ACTION');
    await expect(editor.roles.container).toContainText('ROLE_USER');
});

test('an unknown account leads back to the search', async () => {
    await accounts.findUnknown(anAccount().email);

    await expect(accounts.email).toBeVisible();
});

test('a locked account cannot log in until it is unlocked', async ({browser, request}) => {
    const account = await signUpActiveAccount(request);
    await expectLoginSucceeds(browser, account);

    let editor = await accounts.find(account.email);
    await editor.setLocked(true);
    await editor.save();
    await expectLoginFails(browser, account);

    editor = await accounts.find(account.email);
    await expect(editor.accountLocked).toBeChecked();
    await editor.setLocked(false);
    await editor.save();
    await expectLoginSucceeds(browser, account);
});

test('a disabled account cannot log in until it is enabled again', async ({browser, request}) => {
    const account = await signUpActiveAccount(request);
    await expectLoginSucceeds(browser, account);

    let editor = await accounts.find(account.email);
    await editor.setEnabled(false);
    await editor.save();
    await expectLoginFails(browser, account);

    editor = await accounts.find(account.email);
    await expect(editor.enabled).not.toBeChecked();
    await editor.setEnabled(true);
    await editor.save();
    await expectLoginSucceeds(browser, account);
});

test('the RESET_PASSWORD mandatory action asks for a new password at the next login', async ({browser, request}) => {
    const account = await signUpActiveAccount(request);

    const editor = await accounts.find(account.email);
    await editor.setMandatoryAction('RESET_PASSWORD');
    await editor.save();

    const {context, page} = await loginInFreshBrowser(browser, account);
    await expect(page).toHaveURL(/\/change-password$/);
    await context.close();
});

test('grant and revoke a role', async ({request}) => {
    const account = await signUpActiveAccount(request);
    const adminApi = await AdminApi.create(request);

    let editor = await accounts.find(account.email);
    await editor.addRole('VAUTHENTICATOR_ADMIN');
    await editor.save();
    expect((await adminApi.findAccount(account.email)).authorities.sort()).toEqual(['ROLE_USER', 'VAUTHENTICATOR_ADMIN']);

    editor = await accounts.find(account.email);
    await expect(editor.roles.container).toContainText('VAUTHENTICATOR_ADMIN');
    await editor.removeRole('VAUTHENTICATOR_ADMIN');
    await editor.save();
    expect((await adminApi.findAccount(account.email)).authorities).toEqual(['ROLE_USER']);
});

test('the account page links to its password lifecycle rules', async ({request}) => {
    const account = await signUpActiveAccount(request);

    const editor = await accounts.find(account.email);
    const lifecycle = await editor.openPasswordLifecycle();

    await expect(lifecycle.accountRules.account).toHaveValue(account.email);
    await expect(lifecycle.accountRules.rulesHeading).toHaveText(`Rules of ${account.email}`);
    await expect(lifecycle.accountRules.noRules).toBeVisible();
});

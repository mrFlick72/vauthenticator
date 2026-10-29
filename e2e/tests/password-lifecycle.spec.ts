import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';
import {PasswordLifecyclePage} from '../pages/password-lifecycle-page';
import {anAccount, signUpActiveAccount} from '../support/accounts';
import {uniqueId} from '../support/unique';

let lifecycle: PasswordLifecyclePage;

test.beforeEach(async ({page}) => {
    const home = new AdminHomePage(page);
    await home.goto();
    lifecycle = await home.openPasswordLifecycle();
});

test.describe('account rules', () => {
    test('add, replace and remove a rule of an account', async ({request}) => {
        const account = await signUpActiveAccount(request);
        const rules = lifecycle.accountRules;

        await rules.load(account.email);
        await expect(rules.noRules).toBeVisible();

        await rules.saveRule('PASSWORD_RESET', {amount: '2', unit: 'hours'});
        await expect(rules.feedback).toHaveText(`PASSWORD_RESET saved for ${account.email}`);
        await expect(rules.intervalOf('PASSWORD_RESET')).toHaveText('2 hours');

        await rules.saveRule('PASSWORD_RESET', {amount: '3', unit: 'days'});
        await expect(rules.intervalOf('PASSWORD_RESET')).toHaveText('3 days');
        await expect(rules.rule('PASSWORD_RESET')).toHaveCount(1);

        await rules.removeRule('PASSWORD_RESET');
        await expect(rules.feedback).toHaveText(`PASSWORD_RESET removed for ${account.email}`);
        await expect(rules.noRules).toBeVisible();
    });

    test('an account holds one rule per action', async ({request}) => {
        const account = await signUpActiveAccount(request);
        const rules = lifecycle.accountRules;
        await rules.load(account.email);

        await rules.saveRule('PASSWORD_RESET', {amount: '30', unit: 'days'});
        await expect(rules.intervalOf('PASSWORD_RESET')).toHaveText('30 days');
        await rules.saveRule('ACCOUNT_LOCK', {amount: '90', unit: 'minutes'});

        await expect(rules.intervalOf('ACCOUNT_LOCK')).toHaveText('90 minutes');
        await expect(rules.intervalOf('PASSWORD_RESET')).toHaveText('30 days');
    });

    test('loading requires an account email', async () => {
        await lifecycle.accountRules.load('');

        await expect(lifecycle.accountRules.feedback).toHaveText('Type the account email first');
    });

    // reading the rules of an unknown account returns [] by contract; saving a rule for it is a 404
    test('a rule cannot be saved for an unknown account', async () => {
        const unknown = anAccount();
        const rules = lifecycle.accountRules;
        await rules.load(unknown.email);
        await expect(rules.noRules).toBeVisible();

        await rules.saveRule('PASSWORD_RESET', {amount: '1', unit: 'days'});

        await expect(rules.feedback).toHaveText('The account does not exist');
        await expect(rules.noRules).toBeVisible();
    });

    test('the interval must be a positive whole number', async ({request}) => {
        const account = await signUpActiveAccount(request);
        const rules = lifecycle.accountRules;
        await rules.load(account.email);

        await rules.saveRule('PASSWORD_RESET', {amount: '1.5', unit: 'days'});

        await expect(rules.feedback).toHaveText('The interval must be a positive whole number');
        await expect(rules.noRules).toBeVisible();
    });
});

test.describe('bulk rules', () => {
    // a domain of its own for the scenario, so the pattern matches exactly the accounts it signs up
    async function twoAccountsOfOneDomain(request: Parameters<typeof signUpActiveAccount>[0]) {
        const domain = `${uniqueId()}.e2e.example.com`;
        const accounts = [await signUpActiveAccount(request, anAccount(domain)), await signUpActiveAccount(request, anAccount(domain))];
        return {pattern: `*@${domain}`, accounts};
    }

    test('apply and remove a rule for every account matching a pattern', async ({request}) => {
        const {pattern, accounts} = await twoAccountsOfOneDomain(request);
        const bulk = lifecycle.bulkRules;

        await bulk.apply(pattern, 'PASSWORD_RESET', {amount: '7', unit: 'days'});
        await expect(bulk.feedback).toHaveText('PASSWORD_RESET registered for 2 accounts');

        for (const account of accounts) {
            await lifecycle.accountRules.load(account.email);
            await expect(lifecycle.accountRules.intervalOf('PASSWORD_RESET')).toHaveText('7 days');
        }

        await bulk.remove(pattern, 'PASSWORD_RESET');
        await expect(bulk.feedback).toHaveText('PASSWORD_RESET removed from 2 accounts');

        await lifecycle.accountRules.load(accounts[0].email);
        await expect(lifecycle.accountRules.noRules).toBeVisible();
    });

    test('the bulk confirmation states the pattern and can be cancelled', async ({request}) => {
        const {pattern, accounts} = await twoAccountsOfOneDomain(request);
        const bulk = lifecycle.bulkRules;

        await bulk.requestApply(pattern, 'ACCOUNT_LOCK', {amount: '1', unit: 'days'});
        await expect(bulk.confirmation.dialog).toContainText(`Register ACCOUNT_LOCK every 1 day for all accounts matching "${pattern}"?`);
        await bulk.confirmation.cancel();

        await expect(bulk.feedback).toBeHidden();
        await lifecycle.accountRules.load(accounts[0].email);
        await expect(lifecycle.accountRules.noRules).toBeVisible();
    });

    test('a bulk rule requires a pattern', async () => {
        await lifecycle.bulkRules.requestApply('', 'PASSWORD_RESET', {amount: '1', unit: 'days'});

        await expect(lifecycle.bulkRules.feedback).toHaveText('Type an account pattern first, for example *@gmail.com');
        await expect(lifecycle.bulkRules.confirmation.dialog).toBeHidden();
    });
});

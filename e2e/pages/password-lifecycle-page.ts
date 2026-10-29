import {expect, Locator, Page} from '@playwright/test';
import {ConfirmationDialog} from './components/confirmation-dialog';
import {FormSelect} from './components/form-select';
import {StickyHeadTable} from './components/sticky-head-table';

export type PasswordLifecycleAction = 'PASSWORD_RESET' | 'ACCOUNT_LOCK';
export type IntervalUnit = 'minutes' | 'hours' | 'days';
export type Interval = { amount: string; unit: IntervalUnit };

const INTERVAL_COLUMN = 1;
const REMOVE_COLUMN = 5;

// both cards render a feedback Alert: scope everything to its card
function card(page: Page, title: string) {
    return page.locator('div.MuiCard-root').filter({has: page.getByText(title, {exact: true})});
}

class AccountRulesCard {
    readonly card: Locator;
    readonly account: Locator;
    readonly loadButton: Locator;
    readonly feedback: Locator;
    readonly noRules: Locator;
    readonly rulesHeading: Locator;
    readonly action: FormSelect;
    readonly amount: Locator;
    readonly unit: FormSelect;
    readonly saveButton: Locator;
    readonly table: StickyHeadTable;

    constructor(page: Page) {
        this.card = card(page, 'Account rules');
        this.account = this.card.locator('#account');
        this.loadButton = this.card.getByRole('button', {name: 'Load rules'});
        // the "no rules" notice is an info Alert as well, so feedback excludes it
        this.feedback = this.card.getByRole('alert').filter({hasNotText: 'This account has no password lifecycle rules'});
        this.noRules = this.card.getByRole('alert').filter({hasText: 'This account has no password lifecycle rules'});
        this.rulesHeading = this.card.getByRole('heading', {name: /^Rules of /});
        this.action = new FormSelect(page, 'account-rule-action');
        this.amount = this.card.locator('#account-rule-interval-amount');
        this.unit = new FormSelect(page, 'account-rule-interval-unit');
        this.saveButton = this.card.getByRole('button', {name: 'Save rule'});
        this.table = new StickyHeadTable(page);
    }

    rule(action: PasswordLifecycleAction) {
        return this.table.row(action);
    }

    intervalOf(action: PasswordLifecycleAction) {
        return this.table.cell(action, INTERVAL_COLUMN);
    }

    async load(email: string) {
        await this.account.fill(email);
        await this.loadButton.click();
    }

    async saveRule(action: PasswordLifecycleAction, interval: Interval) {
        await this.action.select(action);
        await this.amount.fill(interval.amount);
        await this.unit.select(interval.unit);
        await this.saveButton.click();
    }

    async removeRule(action: PasswordLifecycleAction) {
        await this.table.actionIcon(action, REMOVE_COLUMN).click();
    }
}

class BulkRulesCard {
    readonly card: Locator;
    readonly pattern: Locator;
    readonly action: FormSelect;
    readonly amount: Locator;
    readonly unit: FormSelect;
    readonly applyButton: Locator;
    readonly removeButton: Locator;
    readonly confirmation: ConfirmationDialog;
    readonly feedback: Locator;

    constructor(page: Page) {
        this.card = card(page, 'Bulk rules by account pattern');
        this.pattern = this.card.locator('#account-pattern');
        this.action = new FormSelect(page, 'bulk-rule-action');
        this.amount = this.card.locator('#bulk-rule-interval-amount');
        this.unit = new FormSelect(page, 'bulk-rule-interval-unit');
        this.applyButton = this.card.getByRole('button', {name: 'Apply'});
        this.removeButton = this.card.getByRole('button', {name: 'Remove'});
        this.confirmation = new ConfirmationDialog(page, 'Confirm bulk operation');
        this.feedback = this.card.getByRole('alert');
    }

    private async fill(pattern: string, action: PasswordLifecycleAction, interval?: Interval) {
        await this.pattern.fill(pattern);
        await this.action.select(action);
        if (interval) {
            await this.amount.fill(interval.amount);
            await this.unit.select(interval.unit);
        }
    }

    // apply and remove ask for confirmation; requestApply stops at the dialog
    async requestApply(pattern: string, action: PasswordLifecycleAction, interval: Interval) {
        await this.fill(pattern, action, interval);
        await this.applyButton.click();
    }

    async apply(pattern: string, action: PasswordLifecycleAction, interval: Interval) {
        await this.requestApply(pattern, action, interval);
        await this.confirmation.confirm();
    }

    async remove(pattern: string, action: PasswordLifecycleAction) {
        await this.fill(pattern, action);
        await this.removeButton.click();
        await this.confirmation.confirm();
    }
}

export class PasswordLifecyclePage {
    static readonly PATH = '/secure/admin/password-lifecycle';

    readonly accountRules: AccountRulesCard;
    readonly bulkRules: BulkRulesCard;

    constructor(private readonly page: Page) {
        this.accountRules = new AccountRulesCard(page);
        this.bulkRules = new BulkRulesCard(page);
    }

    async goto() {
        await this.page.goto(PasswordLifecyclePage.PATH);
        await expect(this.accountRules.card).toBeVisible();
    }
}

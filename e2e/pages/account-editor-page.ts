import {expect, Locator, Page} from '@playwright/test';
import {FormSelect} from './components/form-select';
import {PasswordLifecyclePage} from './password-lifecycle-page';

export type MandatoryAction = 'NO_ACTION' | 'RESET_PASSWORD';

export class AccountEditorPage {
    readonly email: Locator;
    // CheckboxesGroup labels each checkbox with its key
    readonly enabled: Locator;
    readonly accountLocked: Locator;
    readonly mandatoryAction: FormSelect;
    readonly roles: FormSelect;
    readonly passwordLifecycleLink: Locator;
    readonly saveButton: Locator;
    readonly backButton: Locator;

    constructor(private readonly page: Page) {
        this.email = page.locator('#email');
        this.enabled = page.getByLabel('enabled', {exact: true});
        this.accountLocked = page.getByLabel('accountLocked', {exact: true});
        this.mandatoryAction = new FormSelect(page, 'mandatoryAction');
        this.roles = new FormSelect(page, 'roles');
        this.passwordLifecycleLink = page.getByRole('link', {name: 'Password lifecycle rules'});
        this.saveButton = page.getByRole('button', {name: 'Save'});
        this.backButton = page.getByRole('button', {name: 'Back'});
    }

    // the account loads after the page renders and then overwrites the form: every account has ROLE_USER,
    // so its chip shows the load has landed
    async waitForAccount() {
        await expect(this.roles.container).toContainText('ROLE_USER');
    }

    async setEnabled(enabled: boolean) {
        await this.enabled.setChecked(enabled);
    }

    async setLocked(locked: boolean) {
        await this.accountLocked.setChecked(locked);
    }

    async setMandatoryAction(action: MandatoryAction) {
        await this.mandatoryAction.select(action);
    }

    async addRole(role: string) {
        await this.roles.select(role);
    }

    async removeRole(role: string) {
        await this.roles.remove(role);
    }

    // saving goes back to the page the editor was opened from
    async save() {
        await this.saveButton.click();
        await expect(this.page).not.toHaveURL(/\/accounts\/edit\//);
    }

    async openPasswordLifecycle() {
        await this.passwordLifecycleLink.click();
        await expect(this.page).toHaveURL(/\/password-lifecycle\?account=/);
        return new PasswordLifecyclePage(this.page);
    }
}

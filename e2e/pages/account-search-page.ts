import {expect, Locator, Page} from '@playwright/test';
import {AccountEditorPage} from './account-editor-page';

export class AccountSearchPage {
    static readonly PATH = '/secure/admin/accounts';

    readonly email: Locator;
    readonly findButton: Locator;

    constructor(private readonly page: Page) {
        this.email = page.locator('#email');
        this.findButton = page.getByRole('button', {name: 'Find'});
    }

    async goto() {
        await this.page.goto(AccountSearchPage.PATH);
    }

    async find(email: string) {
        const editor = await this.openEditor(email);
        await editor.waitForAccount();
        return editor;
    }

    // opens the editor without waiting for the account to load
    async openEditor(email: string) {
        await this.email.fill(email);
        await this.findButton.click();
        await expect(this.page).toHaveURL(new RegExp(`/accounts/edit/${email}$`));
        return new AccountEditorPage(this.page);
    }

    // an unknown account sends the editor straight back here
    async findUnknown(email: string) {
        await this.email.fill(email);
        await this.findButton.click();
        await expect(this.page).toHaveURL(/\/accounts$/);
    }
}

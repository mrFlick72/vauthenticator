import {expect, Locator, Page} from '@playwright/test';

// the ConfirmationDialog component: a titled Yes / No dialog
export class ConfirmationDialog {
    readonly dialog: Locator;

    constructor(page: Page, title: string | RegExp) {
        this.dialog = page.getByRole('dialog', {name: title});
    }

    async confirm() {
        await this.dialog.getByRole('button', {name: 'Yes'}).click();
        await expect(this.dialog).toBeHidden();
    }

    async cancel() {
        await this.dialog.getByRole('button', {name: 'No'}).click();
        await expect(this.dialog).toBeHidden();
    }
}

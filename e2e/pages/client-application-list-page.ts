import {expect, Locator, Page} from '@playwright/test';
import {ClientApplicationEditorPage} from './client-application-editor-page';

export class ClientApplicationListPage {
    readonly newClientApplicationButton: Locator;

    constructor(private readonly page: Page) {
        this.newClientApplicationButton = page.getByRole('button', {name: 'New Client Application'});
    }

    // StickyHeadTable body rows carry role="checkbox", so getByRole('row') does not match them
    row(clientAppId: string) {
        return this.page.locator('tr').filter({hasText: clientAppId});
    }

    async newClientApplication() {
        await this.newClientApplicationButton.click();
        await expect(this.page).toHaveURL(/\/client-applications\/save$/);
        return new ClientApplicationEditorPage(this.page);
    }

    // the "Edit Application" column (5th) holds a bare icon with no accessible name
    async edit(clientAppId: string) {
        await this.row(clientAppId).locator('td').nth(4).locator('svg').click();
        await expect(this.page).toHaveURL(new RegExp(`/client-applications/edit/${clientAppId}$`));
        return new ClientApplicationEditorPage(this.page);
    }
}

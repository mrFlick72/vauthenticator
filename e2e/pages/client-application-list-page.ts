import {expect, Locator, Page} from '@playwright/test';
import {ClientApplication, ClientApplicationEditorPage} from './client-application-editor-page';

export class ClientApplicationListPage {
    readonly newClientApplicationButton: Locator;
    readonly deleteDialog: Locator;
    readonly resetSecretDialog: Locator;
    readonly rowsPerPage: Locator;

    constructor(private readonly page: Page) {
        this.newClientApplicationButton = page.getByRole('button', {name: 'New Client Application'});
        this.deleteDialog = page.getByRole('dialog', {name: 'Client App deletion'});
        this.resetSecretDialog = page.getByRole('dialog', {name: /Reset Client App/});
        this.rowsPerPage = page.getByRole('combobox', {name: 'Rows per page:'});
    }

    // the table paginates at 10 rows and resets to it on every visit: show the largest page so a newly
    // created client application is on screen whatever else the database holds
    async showAllRows() {
        await this.rowsPerPage.click();
        await this.page.getByRole('option', {name: '100'}).click();
        await expect(this.rowsPerPage).toHaveText('100');
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

    async createClientApplication(clientApplication: ClientApplication) {
        const editor = await this.newClientApplication();
        await editor.fill(clientApplication);
        await editor.save();
        await expect(this.row(clientApplication.clientAppId)).toBeVisible();
    }

    // the edit, delete and reset secret columns (5th to 7th) hold bare icons with no accessible name,
    // and the production build of MUI icons carries no data-testid
    private actionIcon(clientAppId: string, column: number) {
        return this.row(clientAppId).locator('td').nth(column).locator('svg');
    }

    async edit(clientAppId: string) {
        await this.actionIcon(clientAppId, 4).click();
        await expect(this.page).toHaveURL(new RegExp(`/client-applications/edit/${clientAppId}$`));
        return new ClientApplicationEditorPage(this.page);
    }

    async openDeleteDialog(clientAppId: string) {
        await this.actionIcon(clientAppId, 5).click();
        await expect(this.deleteDialog).toBeVisible();
    }

    async delete(clientAppId: string) {
        await this.openDeleteDialog(clientAppId);
        await this.deleteDialog.getByRole('button', {name: 'Yes'}).click();
        await expect(this.deleteDialog).toBeHidden();
    }

    async cancelDelete(clientAppId: string) {
        await this.openDeleteDialog(clientAppId);
        await this.deleteDialog.getByRole('button', {name: 'No'}).click();
        await expect(this.deleteDialog).toBeHidden();
    }

    async resetSecret(clientAppId: string, secret: string) {
        await this.actionIcon(clientAppId, 6).click();
        await expect(this.resetSecretDialog).toBeVisible();
        await this.resetSecretDialog.locator('#resetSecretKeyField').fill(secret);
        await this.resetSecretDialog.getByRole('button', {name: 'Save'}).click();
        // the dialog closes only once the api call has completed
        await expect(this.resetSecretDialog).toBeHidden();
    }
}

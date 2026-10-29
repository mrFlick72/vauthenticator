import {expect, Locator, Page} from '@playwright/test';
import {ClientApplication, ClientApplicationEditorPage} from './client-application-editor-page';
import {ConfirmationDialog} from './components/confirmation-dialog';
import {StickyHeadTable} from './components/sticky-head-table';

const EDIT_COLUMN = 4;
const DELETE_COLUMN = 5;
const RESET_SECRET_COLUMN = 6;

export class ClientApplicationListPage {
    static readonly PATH = '/secure/admin/client-applications/list';

    readonly newClientApplicationButton: Locator;
    readonly table: StickyHeadTable;
    readonly deleteDialog: ConfirmationDialog;
    readonly resetSecretDialog: Locator;

    constructor(private readonly page: Page) {
        this.newClientApplicationButton = page.getByRole('button', {name: 'New Client Application'});
        this.table = new StickyHeadTable(page);
        this.deleteDialog = new ConfirmationDialog(page, 'Client App deletion');
        this.resetSecretDialog = page.getByRole('dialog', {name: /Reset Client App/});
    }

    async showAllRows() {
        await this.table.showAllRows();
    }

    row(clientAppId: string) {
        return this.table.row(clientAppId);
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

    async edit(clientAppId: string) {
        await this.table.actionIcon(clientAppId, EDIT_COLUMN).click();
        await expect(this.page).toHaveURL(new RegExp(`/client-applications/edit/${clientAppId}$`));
        return new ClientApplicationEditorPage(this.page);
    }

    async delete(clientAppId: string) {
        await this.table.actionIcon(clientAppId, DELETE_COLUMN).click();
        await this.deleteDialog.confirm();
    }

    async cancelDelete(clientAppId: string) {
        await this.table.actionIcon(clientAppId, DELETE_COLUMN).click();
        await this.deleteDialog.cancel();
    }

    async resetSecret(clientAppId: string, secret: string) {
        await this.table.actionIcon(clientAppId, RESET_SECRET_COLUMN).click();
        await expect(this.resetSecretDialog).toBeVisible();
        await this.resetSecretDialog.locator('#resetSecretKeyField').fill(secret);
        await this.resetSecretDialog.getByRole('button', {name: 'Save'}).click();
        // the dialog closes only once the api call has completed
        await expect(this.resetSecretDialog).toBeHidden();
    }
}

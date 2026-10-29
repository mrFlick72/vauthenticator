import {expect, Locator, Page} from '@playwright/test';
import {ConfirmationDialog} from './components/confirmation-dialog';
import {StickyHeadTable} from './components/sticky-head-table';

const DESCRIPTION_COLUMN = 1;
const EDIT_COLUMN = 2;
const DELETE_COLUMN = 3;

export type Role = { name: string; description: string };

export class RolesPage {
    readonly newRoleButton: Locator;
    readonly table: StickyHeadTable;
    readonly roleDialog: Locator;
    readonly name: Locator;
    readonly description: Locator;
    readonly deleteDialog: ConfirmationDialog;

    constructor(private readonly page: Page) {
        this.newRoleButton = page.getByRole('button', {name: 'New Role'});
        this.table = new StickyHeadTable(page);
        this.roleDialog = page.getByRole('dialog', {name: 'Role management'});
        this.name = this.roleDialog.locator('#name');
        this.description = this.roleDialog.locator('#description');
        this.deleteDialog = new ConfirmationDialog(page, 'Role delete');
    }

    row(roleName: string) {
        return this.table.row(roleName);
    }

    descriptionOf(roleName: string) {
        return this.table.cell(roleName, DESCRIPTION_COLUMN);
    }

    // the dialog closes, and the list is refetched, only once the api accepted the role
    async saveDialog() {
        await this.roleDialog.getByRole('button', {name: 'Save'}).click();
        await expect(this.roleDialog).toBeHidden();
    }

    async cancelDialog() {
        await this.roleDialog.getByRole('button', {name: 'Cancel'}).click();
        await expect(this.roleDialog).toBeHidden();
    }

    async create(role: Role) {
        await this.newRoleButton.click();
        await expect(this.roleDialog).toBeVisible();
        await this.name.fill(role.name);
        await this.description.fill(role.description);
        await this.saveDialog();
    }

    async openEditor(roleName: string) {
        await this.table.actionIcon(roleName, EDIT_COLUMN).click();
        await expect(this.roleDialog).toBeVisible();
    }

    async changeDescription(roleName: string, description: string) {
        await this.openEditor(roleName);
        await this.description.fill(description);
        await this.saveDialog();
    }

    // resolves to the http status of the delete call: the failure snackbar hides after 600ms, too soon to assert
    async delete(roleName: string) {
        await this.table.actionIcon(roleName, DELETE_COLUMN).click();
        const response = this.page.waitForResponse(r => r.request().method() === 'DELETE' && r.url().includes('/api/roles/'));
        await this.deleteDialog.confirm();
        return (await response).status();
    }

    async cancelDelete(roleName: string) {
        await this.table.actionIcon(roleName, DELETE_COLUMN).click();
        await this.deleteDialog.cancel();
    }
}

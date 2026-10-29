import {expect, Locator, Page, Response} from '@playwright/test';
import {StickyHeadTable} from './components/sticky-head-table';

const DELETE_COLUMN = 2;
const ROTATE_COLUMN = 3;
const EXPIRE_IN_COLUMN = 4;

export class KeysPage {
    static readonly PATH = '/secure/admin/keys';

    readonly table: StickyHeadTable;
    readonly deleteDialog: Locator;
    readonly rotateDialog: Locator;

    constructor(private readonly page: Page) {
        this.table = new StickyHeadTable(page);
        this.deleteDialog = page.getByRole('dialog', {name: 'Delete Key Dialog'});
        this.rotateDialog = page.getByRole('dialog', {name: 'Renew Key Dialog'});
    }

    async goto() {
        await this.page.goto(KeysPage.PATH);
    }

    row(kid: string) {
        return this.table.row(kid);
    }

    expireIn(kid: string) {
        return this.table.cell(kid, EXPIRE_IN_COLUMN);
    }

    private apiCall(method: string, path: string) {
        return this.page.waitForResponse(r => r.request().method() === method && new URL(r.url()).pathname === path);
    }

    // both dialogs confirm with "Save"; the result snackbars hide after 600ms, so callers get the http status
    private async confirm(dialog: Locator, call: Promise<Response>) {
        await dialog.getByRole('button', {name: 'Save'}).click();
        await expect(dialog).toBeHidden();
        return (await call).status();
    }

    async rotate(kid: string) {
        await this.table.actionIcon(kid, ROTATE_COLUMN).click();
        await expect(this.rotateDialog).toBeVisible();
        return this.confirm(this.rotateDialog, this.apiCall('POST', '/api/keys/rotate'));
    }

    // the page refuses to rotate a key twice without calling the api: report whether a call went out
    async cancelRotation(kid: string) {
        await this.table.actionIcon(kid, ROTATE_COLUMN).click();
        await expect(this.rotateDialog).toBeVisible();
        await this.rotateDialog.getByRole('button', {name: 'Cancel'}).click();
        await expect(this.rotateDialog).toBeHidden();
    }

    async rotateAlreadyRotated(kid: string) {
        let called = false;
        const listener = (request: { method(): string; url(): string }) => {
            if (request.method() === 'POST' && request.url().endsWith('/api/keys/rotate')) {
                called = true;
            }
        };
        this.page.on('request', listener);
        await this.table.actionIcon(kid, ROTATE_COLUMN).click();
        await expect(this.rotateDialog).toBeVisible();
        await this.rotateDialog.getByRole('button', {name: 'Save'}).click();
        await expect(this.rotateDialog).toBeHidden();
        this.page.off('request', listener);
        return called;
    }

    async delete(kid: string) {
        await this.table.actionIcon(kid, DELETE_COLUMN).click();
        await expect(this.deleteDialog).toBeVisible();
        return this.confirm(this.deleteDialog, this.apiCall('DELETE', '/api/keys'));
    }
}

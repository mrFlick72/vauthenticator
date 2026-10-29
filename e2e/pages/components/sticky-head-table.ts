import {expect, Locator, Page} from '@playwright/test';

// the StickyHeadTable component behind every list in the management ui
export class StickyHeadTable {
    readonly rowsPerPage: Locator;

    constructor(private readonly page: Page) {
        this.rowsPerPage = page.getByRole('combobox', {name: 'Rows per page:'});
    }

    // the table paginates at 10 rows and resets to it on every visit: show the largest page so a row
    // created by the scenario is on screen whatever else the database holds
    async showAllRows() {
        await this.rowsPerPage.click();
        await this.page.getByRole('option', {name: '100'}).click();
        await expect(this.rowsPerPage).toHaveText('100');
    }

    // body rows carry role="checkbox", so getByRole('row') does not match them
    row(text: string) {
        return this.page.locator('tr').filter({hasText: text});
    }

    cell(text: string, column: number) {
        return this.row(text).locator('td').nth(column);
    }

    // action columns hold bare icons with no accessible name, and the production build of MUI icons
    // carries no data-testid
    actionIcon(text: string, column: number) {
        return this.cell(text, column).locator('svg');
    }
}

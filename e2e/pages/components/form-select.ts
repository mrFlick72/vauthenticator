import {Locator, Page} from '@playwright/test';

// the FormSelect component, a react-select widget
export class FormSelect {
    readonly container: Locator;

    constructor(private readonly page: Page, id: string) {
        // FormSelect renders its label and its react-select container with the same id
        this.container = page.locator(`div#${id}`);
    }

    // type the option, then pick the highlighted match with Enter
    async select(option: string) {
        await this.container.locator('input').fill(option);
        await this.page.keyboard.press('Enter');
    }

    // a multi select shows one removable chip per selected option
    async remove(option: string) {
        await this.container.getByRole('button', {name: `Remove ${option}`}).click();
    }
}

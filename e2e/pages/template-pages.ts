import {expect, Locator, Page} from '@playwright/test';
import {FormSelect} from './components/form-select';

// the email and the SMS template pages share one layout: a type select, a contenteditable body and Save
class TemplatePage {
    readonly type: FormSelect;
    readonly body: Locator;
    readonly saveButton: Locator;
    readonly savedAlert: Locator;

    constructor(protected readonly page: Page, private readonly path: string, typeSelectId: string, savedMessage: string) {
        this.type = new FormSelect(page, typeSelectId);
        this.body = page.locator('[contenteditable="true"]');
        this.saveButton = page.getByRole('button', {name: 'Save'});
        this.savedAlert = page.getByRole('alert').filter({hasText: savedMessage});
    }

    async goto() {
        await this.page.goto(this.path);
    }

    // the body is fetched after the type is picked and then replaces whatever the editor holds:
    // wait for it before reading or typing
    async load(templateType: string) {
        const loaded = this.page.waitForResponse(r => r.request().method() === 'GET' && r.url().endsWith(`/${templateType}`));
        await this.type.select(templateType);
        await loaded;
        await expect(this.body).not.toBeEmpty();
        return this.body.innerText();
    }

    // replaces the body by typing a single line: fill() on a contenteditable turns every line into a block,
    // which innerText reads back with extra blank lines, and the caret position after a load is not reliable
    async replaceBody(text: string) {
        await this.body.click();
        await this.page.keyboard.press('ControlOrMeta+A');
        await this.page.keyboard.insertText(text);
        await expect(this.body).toHaveText(text);
    }

    async save() {
        await this.saveButton.click();
        await expect(this.savedAlert).toBeVisible();
    }
}

export class EmailTemplatePage extends TemplatePage {
    static readonly PATH = '/secure/admin/email-templates';

    constructor(page: Page) {
        super(page, EmailTemplatePage.PATH, 'email-template-type', 'EMail Template save succeeded');
    }
}

export class SmsTemplatePage extends TemplatePage {
    static readonly PATH = '/secure/admin/sms-templates';

    constructor(page: Page) {
        super(page, SmsTemplatePage.PATH, 'sms-template-type', 'SMS Template save succeeded');
    }
}

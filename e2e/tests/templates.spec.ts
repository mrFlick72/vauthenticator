import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';
import {EmailTemplatePage, SmsTemplatePage} from '../pages/template-pages';
import {AdminApi, TemplateKind} from '../support/admin-api';

const collapseWhitespace = (text: string) => text.replace(/\s+/g, ' ').trim();

// templates are global to the auth-server: only the MFA templates are edited, nothing in the suite sends
// an MFA message, and each scenario puts the original back through the api
async function expectEditable(
    page: { reload(): Promise<unknown> },
    adminApi: AdminApi,
    kind: TemplateKind,
    templates: EmailTemplatePage | SmsTemplatePage,
) {
    const original = await adminApi.template(kind, 'MFA');
    const changed = `E2E change ${Date.now()} {{ mfaCode }}`;
    try {
        // the editor is a plain contenteditable div: its innerText drops the indentation of the template lines
        expect(collapseWhitespace(await templates.load('MFA'))).toBe(collapseWhitespace(original));
        await templates.replaceBody(changed);
        await templates.save();

        expect(await adminApi.template(kind, 'MFA')).toBe(changed);

        await page.reload();
        expect(await templates.load('MFA')).toBe(changed);
    } finally {
        await adminApi.saveTemplate(kind, 'MFA', original);
    }
}

test('edit the MFA email template', async ({page, request}) => {
    const home = new AdminHomePage(page);
    await home.goto();

    await expectEditable(page, await AdminApi.create(request), 'email', await home.openEmailTemplates());
});

// read only: these templates are sent by flows other scenarios run, so they are never edited
for (const emailType of ['WELCOME', 'EMAIL_VERIFICATION', 'RESET_PASSWORD']) {
    test(`load the ${emailType} email template`, async ({page, request}) => {
        const adminApi = await AdminApi.create(request);
        const home = new AdminHomePage(page);
        await home.goto();
        const templates = await home.openEmailTemplates();

        expect(collapseWhitespace(await templates.load(emailType))).toBe(collapseWhitespace(await adminApi.template('email', emailType)));
    });
}

test('edit the MFA SMS template', async ({page, request}) => {
    const home = new AdminHomePage(page);
    await home.goto();

    await expectEditable(page, await AdminApi.create(request), 'sms', await home.openSmsTemplates());
});

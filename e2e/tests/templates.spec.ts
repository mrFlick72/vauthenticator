import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';
import {EmailTemplatePage, SmsTemplatePage} from '../pages/template-pages';
import {AdminApi, TemplateKind} from '../support/admin-api';

// templates are global to the auth-server: only the MFA templates are edited, nothing in the suite sends
// an MFA message, and each scenario puts the original back through the api
async function expectEditable(
    page: { reload(): Promise<unknown> },
    adminApi: AdminApi,
    kind: TemplateKind,
    templates: EmailTemplatePage | SmsTemplatePage,
) {
    const original = await adminApi.template(kind, 'MFA');
    // several indented lines: the editor must keep the indentation and add no blank lines
    const changed = `E2E change ${Date.now()}\n    {{ mfaCode }}\n        indented twice\n`;
    try {
        await templates.load('MFA');
        await templates.replaceBody(changed);
        await templates.save();

        expect(await adminApi.template(kind, 'MFA')).toBe(changed);

        await page.reload();
        await templates.load('MFA');
        await expect(templates.body).toContainText('indented twice');
    } finally {
        await adminApi.saveTemplate(kind, 'MFA', original);
    }
}

test('saving the MFA email template unchanged keeps its exact text', async ({page, request}) => {
    const adminApi = await AdminApi.create(request);
    const original = await adminApi.template('email', 'MFA');
    const home = new AdminHomePage(page);
    await home.goto();
    const templates = await home.openEmailTemplates();
    try {
        await templates.load('MFA');
        await templates.save();

        expect(await adminApi.template('email', 'MFA')).toBe(original);
    } finally {
        await adminApi.saveTemplate('email', 'MFA', original);
    }
});

test('undo after loading a template does not bring back the previous one', async ({page, request}) => {
    const adminApi = await AdminApi.create(request);
    const original = await adminApi.template('email', 'MFA');
    const home = new AdminHomePage(page);
    await home.goto();
    const templates = await home.openEmailTemplates();
    try {
        await templates.load('WELCOME');
        await templates.load('MFA');
        await templates.undo();
        await templates.save();

        expect(await adminApi.template('email', 'MFA')).toBe(original);
    } finally {
        await adminApi.saveTemplate('email', 'MFA', original);
    }
});

test('Escape then Tab moves the focus out of the template editor', async ({page}) => {
    const home = new AdminHomePage(page);
    await home.goto();
    const templates = await home.openEmailTemplates();
    await templates.load('MFA');

    await templates.leaveEditorByKeyboard();

    await expect(templates.body).not.toBeFocused();
});

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

        const title = (await adminApi.template('email', emailType)).match(/<title>(.*)<\/title>/)![1];
        await templates.load(emailType);

        await expect(templates.body).toContainText(`<title>${title}</title>`);
    });
}

test('edit the MFA SMS template', async ({page, request}) => {
    const home = new AdminHomePage(page);
    await home.goto();

    await expectEditable(page, await AdminApi.create(request), 'sms', await home.openSmsTemplates());
});

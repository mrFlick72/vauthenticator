import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';
import {ClientApplicationListPage} from '../pages/client-application-list-page';
import {aClientApplication, requestClientCredentialsToken} from '../support/client-applications';

let clientApplications: ClientApplicationListPage;

test.beforeEach(async ({page}) => {
    const adminHome = new AdminHomePage(page);
    await adminHome.goto();
    clientApplications = await adminHome.openClientApplications();
});

test('create a new client application', async () => {
    const clientApplication = aClientApplication();

    const editor = await clientApplications.newClientApplication();
    await editor.fill(clientApplication);
    await editor.save();

    await expect(clientApplications.row(clientApplication.clientAppId)).toBeVisible();

    // reopen it to prove the values made it through the api and back
    const saved = await clientApplications.edit(clientApplication.clientAppId);
    await expect(saved.clientAppName).toHaveValue(clientApplication.clientAppName);
    await expect(saved.applicationType.container).toContainText('CONFIDENTIAL');
    await saved.openUriTab();
    await expect(saved.webServerRedirectUri).toHaveValue(clientApplication.webServerRedirectUri);
});

test('create a client application with a generated id and secret', async () => {
    const editor = await clientApplications.newClientApplication();
    await editor.generateClientAppId();
    await expect(editor.clientAppId).toHaveValue(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/);
    await editor.generateSecret();
    await expect(editor.secret).not.toHaveValue('');

    const clientAppId = await editor.clientAppId.inputValue();
    const clientApplication = aClientApplication({clientAppId});
    await editor.clientAppName.fill(clientApplication.clientAppName);
    await editor.nextTab();
    await editor.fillPermissions(clientApplication);
    await editor.nextTab();
    await editor.fillUris(clientApplication);
    await editor.save();

    await expect(clientApplications.row(clientAppId)).toBeVisible();
});

test('create a public client application with pkce', async () => {
    const clientApplication = aClientApplication({applicationType: 'PUBLIC', secret: undefined, withPkce: true});

    const editor = await clientApplications.newClientApplication();
    await editor.fillCredentials(clientApplication);
    await expect(editor.secret).toBeHidden();
    await editor.nextTab();
    await editor.fillPermissions(clientApplication);
    await editor.nextTab();
    await editor.fillUris(clientApplication);
    await editor.save();

    const saved = await clientApplications.edit(clientApplication.clientAppId);
    await expect(saved.applicationType.container).toContainText('PUBLIC');
    await expect(saved.secret).toBeHidden();
    await saved.openPermissionTab();
    await expect(saved.withPkce).toBeChecked();
});

test('edit a client application', async () => {
    const clientApplication = aClientApplication();
    await clientApplications.createClientApplication(clientApplication);
    const renamed = `${clientApplication.clientAppName} renamed`;

    const editor = await clientApplications.edit(clientApplication.clientAppId);
    await expect(editor.clientAppName).toHaveValue(clientApplication.clientAppName);
    await editor.clientAppName.fill(renamed);
    await editor.openPermissionTab();
    await editor.grantType('client_credentials').check();
    await editor.accessTokenValidity.fill('7200');
    await editor.save();

    await expect(clientApplications.row(clientApplication.clientAppId)).toContainText(renamed);
    await expect(clientApplications.row(clientApplication.clientAppId)).toContainText('client_credentials');

    const saved = await clientApplications.edit(clientApplication.clientAppId);
    await expect(saved.clientAppName).toHaveValue(renamed);
    await saved.openPermissionTab();
    await expect(saved.grantType('client_credentials')).toBeChecked();
    await expect(saved.accessTokenValidity).toHaveValue('7200');
});

test('cancelling the deletion keeps the client application', async () => {
    const clientApplication = aClientApplication();
    await clientApplications.createClientApplication(clientApplication);

    await clientApplications.cancelDelete(clientApplication.clientAppId);

    await expect(clientApplications.row(clientApplication.clientAppId)).toBeVisible();
});

test('delete a client application', async ({page}) => {
    const clientApplication = aClientApplication();
    await clientApplications.createClientApplication(clientApplication);

    await clientApplications.delete(clientApplication.clientAppId);

    await expect(clientApplications.row(clientApplication.clientAppId)).toHaveCount(0);
    // the list is refetched after a delete: a reload proves it is gone from the api too
    await page.reload();
    await clientApplications.showAllRows();
    await expect(clientApplications.row(clientApplication.clientAppId)).toHaveCount(0);
});

test('reset the secret of a client application', async ({request}) => {
    const clientApplication = aClientApplication({authorizedGrantTypes: ['client_credentials']});
    await clientApplications.createClientApplication(clientApplication);
    expect((await requestClientCredentialsToken(request, clientApplication.clientAppId, clientApplication.secret!)).ok()).toBe(true);

    const newSecret = 'new-secret!';
    await clientApplications.resetSecret(clientApplication.clientAppId, newSecret);

    expect((await requestClientCredentialsToken(request, clientApplication.clientAppId, clientApplication.secret!)).status()).toBe(401);
    expect((await requestClientCredentialsToken(request, clientApplication.clientAppId, newSecret)).ok()).toBe(true);
});

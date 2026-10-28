import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';

test('create a new client application', async ({page}) => {
    const clientAppId = `e2e-${Date.now()}`;
    const clientAppName = `E2E client application ${clientAppId}`;
    const origin = `https://${clientAppId}.example.com`;
    const redirectUri = `${origin}/callback`;

    const adminHome = new AdminHomePage(page);
    await adminHome.goto();
    const clientApplications = await adminHome.openClientApplications();

    const editor = await clientApplications.newClientApplication();
    await editor.fillCredentials({clientAppId, secret: 'secret', clientAppName});
    await editor.nextTab();
    await editor.fillPermissions({
        scopes: ['openid'],
        authorizedGrantTypes: ['authorization_code'],
        accessTokenValidity: 3600,
        refreshTokenValidity: 3600,
    });
    await editor.nextTab();
    await editor.fillUris({
        webServerRedirectUri: redirectUri,
        allowedOrigins: origin,
        postLogoutRedirectUri: origin,
        logoutUri: `${origin}/logout`,
    });
    await editor.save();

    await expect(clientApplications.row(clientAppId)).toBeVisible();

    // reopen it to prove the values made it through the api and back
    const saved = await clientApplications.edit(clientAppId);
    await expect(saved.clientAppName).toHaveValue(clientAppName);
    await saved.openUriTab();
    await expect(saved.webServerRedirectUri).toHaveValue(redirectUri);
});

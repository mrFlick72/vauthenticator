import {expect, Locator, Page} from '@playwright/test';
import {ClientApplicationListPage} from './client-application-list-page';

export type ClientApplicationCredentials = {
    clientAppId: string;
    secret: string;
    clientAppName: string;
};

export type ClientApplicationPermissions = {
    scopes: string[];
    authorizedGrantTypes: string[];
    accessTokenValidity: number;
    refreshTokenValidity: number;
};

export type ClientApplicationUris = {
    webServerRedirectUri: string;
    allowedOrigins: string;
    postLogoutRedirectUri: string;
    logoutUri: string;
};

// the same three-tab form serves /client-applications/save and /client-applications/edit/:id
export class ClientApplicationEditorPage {
    readonly clientAppId: Locator;
    readonly secret: Locator;
    readonly clientAppName: Locator;
    readonly scopes: Locator;
    readonly accessTokenValidity: Locator;
    readonly refreshTokenValidity: Locator;
    readonly webServerRedirectUri: Locator;
    readonly allowedOrigins: Locator;
    readonly postLogoutRedirectUri: Locator;
    readonly logoutUri: Locator;
    readonly nextTabButton: Locator;
    readonly saveButton: Locator;
    readonly uriTab: Locator;

    constructor(private readonly page: Page) {
        this.clientAppId = page.locator('#clientAppId');
        this.secret = page.locator('#secret');
        this.clientAppName = page.locator('#clientAppName');
        this.scopes = page.locator('#scopes input');
        this.accessTokenValidity = page.locator('#accessTokenValidity');
        this.refreshTokenValidity = page.locator('#refreshTokenValidity');
        this.webServerRedirectUri = page.locator('#webServerRedirectUri');
        this.allowedOrigins = page.locator('#allowedOrigins');
        this.postLogoutRedirectUri = page.locator('#postLogoutRedirectUri');
        this.logoutUri = page.locator('#logoutUri');
        this.nextTabButton = page.getByRole('button', {name: 'Next Tab'});
        this.saveButton = page.getByRole('button', {name: 'Save Client Application'});
        this.uriTab = page.getByRole('tab', {name: /Uri Section/});
    }

    async fillCredentials(credentials: ClientApplicationCredentials) {
        await this.clientAppId.fill(credentials.clientAppId);
        await this.secret.fill(credentials.secret);
        await this.clientAppName.fill(credentials.clientAppName);
    }

    async fillPermissions(permissions: ClientApplicationPermissions) {
        for (const scope of permissions.scopes) {
            await this.scopes.fill(scope);
            await this.page.keyboard.press('Enter');
        }
        for (const grantType of permissions.authorizedGrantTypes) {
            await this.page.getByLabel(grantType).check();
        }
        await this.accessTokenValidity.fill(String(permissions.accessTokenValidity));
        await this.refreshTokenValidity.fill(String(permissions.refreshTokenValidity));
    }

    async fillUris(uris: ClientApplicationUris) {
        await this.webServerRedirectUri.fill(uris.webServerRedirectUri);
        await this.allowedOrigins.fill(uris.allowedOrigins);
        await this.postLogoutRedirectUri.fill(uris.postLogoutRedirectUri);
        await this.logoutUri.fill(uris.logoutUri);
    }

    async nextTab() {
        await this.nextTabButton.click();
    }

    async openUriTab() {
        await this.uriTab.click();
    }

    async save() {
        await this.saveButton.click();
        await expect(this.page).toHaveURL(/\/client-applications\/list$/);
        return new ClientApplicationListPage(this.page);
    }
}

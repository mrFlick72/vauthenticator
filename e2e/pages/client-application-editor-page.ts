import {expect, Locator, Page} from '@playwright/test';
import {ClientApplicationListPage} from './client-application-list-page';

export type ClientApplicationType = 'CONFIDENTIAL' | 'PUBLIC';
export type AuthorizedGrantType = 'authorization_code' | 'refresh_token' | 'client_credentials';

export type ClientApplicationCredentials = {
    clientAppId: string;
    applicationType?: ClientApplicationType;
    // a public client application has no secret
    secret?: string;
    clientAppName: string;
};

export type ClientApplicationPermissions = {
    scopes: string[];
    withPkce?: boolean;
    authorizedGrantTypes: AuthorizedGrantType[];
    accessTokenValidity: number;
    refreshTokenValidity: number;
};

export type ClientApplicationUris = {
    webServerRedirectUri: string;
    allowedOrigins: string;
    postLogoutRedirectUri: string;
    logoutUri: string;
};

export type ClientApplication = ClientApplicationCredentials & ClientApplicationPermissions & ClientApplicationUris;

// the same three-tab form serves /client-applications/save and /client-applications/edit/:id
export class ClientApplicationEditorPage {
    readonly clientAppId: Locator;
    readonly applicationType: Locator;
    readonly secret: Locator;
    readonly clientAppName: Locator;
    readonly scopes: Locator;
    readonly withPkce: Locator;
    readonly accessTokenValidity: Locator;
    readonly refreshTokenValidity: Locator;
    readonly webServerRedirectUri: Locator;
    readonly allowedOrigins: Locator;
    readonly postLogoutRedirectUri: Locator;
    readonly logoutUri: Locator;
    // id and secret generators are bare icons next to their fields, with no accessible name
    readonly generateClientAppIdButton: Locator;
    readonly generateSecretButton: Locator;
    readonly generatedSecretAlert: Locator;
    readonly nextTabButton: Locator;
    readonly saveButton: Locator;
    readonly permissionTab: Locator;
    readonly uriTab: Locator;

    constructor(private readonly page: Page) {
        this.clientAppId = page.locator('#clientAppId');
        this.applicationType = page.locator('#applicationType');
        this.secret = page.locator('#secret');
        this.clientAppName = page.locator('#clientAppName');
        this.scopes = page.locator('#scopes');
        this.withPkce = page.getByLabel('with_pkce');
        this.accessTokenValidity = page.locator('#accessTokenValidity');
        this.refreshTokenValidity = page.locator('#refreshTokenValidity');
        this.webServerRedirectUri = page.locator('#webServerRedirectUri');
        this.allowedOrigins = page.locator('#allowedOrigins');
        this.postLogoutRedirectUri = page.locator('#postLogoutRedirectUri');
        this.logoutUri = page.locator('#logoutUri');
        // the icon is the input's end adornment, a sibling of the <input> inside the same MUI input root
        this.generateClientAppIdButton = this.clientAppId.locator('..').locator('svg');
        this.generateSecretButton = this.secret.locator('..').locator('svg');
        this.generatedSecretAlert = page.getByRole('alert').filter({hasText: 'Secret:'});
        this.nextTabButton = page.getByRole('button', {name: 'Next Tab'});
        this.saveButton = page.getByRole('button', {name: 'Save Client Application'});
        this.permissionTab = page.getByRole('tab', {name: /Permission Definition Section/});
        this.uriTab = page.getByRole('tab', {name: /Uri Section/});
    }

    grantType(grantType: AuthorizedGrantType) {
        return this.page.getByLabel(grantType);
    }

    // scopes and application type are react-select widgets: type the option, then pick it with Enter
    private async selectOption(select: Locator, option: string) {
        await select.locator('input').fill(option);
        await this.page.keyboard.press('Enter');
    }

    async fillCredentials(credentials: ClientApplicationCredentials) {
        await this.clientAppId.fill(credentials.clientAppId);
        if (credentials.applicationType) {
            await this.selectOption(this.applicationType, credentials.applicationType);
        }
        if (credentials.secret !== undefined) {
            await this.secret.fill(credentials.secret);
        }
        await this.clientAppName.fill(credentials.clientAppName);
    }

    async generateClientAppId() {
        await this.generateClientAppIdButton.click();
    }

    async generateSecret() {
        await this.generateSecretButton.click();
        await expect(this.generatedSecretAlert).toBeVisible();
    }

    async fillPermissions(permissions: ClientApplicationPermissions) {
        for (const scope of permissions.scopes) {
            await this.selectOption(this.scopes, scope);
        }
        if (permissions.withPkce !== undefined) {
            await this.withPkce.setChecked(permissions.withPkce);
        }
        for (const grantType of permissions.authorizedGrantTypes) {
            await this.grantType(grantType).check();
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

    // walks the three tabs in order and saves
    async fill(clientApplication: ClientApplication) {
        await this.fillCredentials(clientApplication);
        await this.nextTab();
        await this.fillPermissions(clientApplication);
        await this.nextTab();
        await this.fillUris(clientApplication);
    }

    async nextTab() {
        await this.nextTabButton.click();
    }

    async openPermissionTab() {
        await this.permissionTab.click();
    }

    async openUriTab() {
        await this.uriTab.click();
    }

    async save() {
        await this.saveButton.click();
        await expect(this.page).toHaveURL(/\/client-applications\/list$/);
        const clientApplications = new ClientApplicationListPage(this.page);
        await clientApplications.showAllRows();
        return clientApplications;
    }
}

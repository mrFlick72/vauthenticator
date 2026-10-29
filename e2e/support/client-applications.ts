import {APIRequestContext} from '@playwright/test';
import {AUTH_SERVER_URL} from '../playwright.config';
import {ClientApplication} from '../pages/client-application-editor-page';
import {basic} from './admin-api';
import {uniqueId} from './unique';

export function aClientApplication(overrides: Partial<ClientApplication> = {}): ClientApplication {
    const clientAppId = overrides.clientAppId ?? uniqueId();
    const origin = `https://${clientAppId}.example.com`;
    return {
        clientAppId,
        secret: 'secret',
        clientAppName: `E2E client application ${clientAppId}`,
        scopes: ['openid'],
        authorizedGrantTypes: ['authorization_code'],
        accessTokenValidity: 3600,
        refreshTokenValidity: 3600,
        webServerRedirectUri: `${origin}/callback`,
        allowedOrigins: origin,
        postLogoutRedirectUri: origin,
        logoutUri: `${origin}/logout`,
        ...overrides,
    };
}

// proves a client secret against the auth-server itself rather than through the management ui
export function requestClientCredentialsToken(request: APIRequestContext, clientAppId: string, secret: string) {
    return request.post(`${AUTH_SERVER_URL}/oauth2/token`, {
        form: {grant_type: 'client_credentials'},
        headers: {Authorization: basic(clientAppId, secret)},
    });
}

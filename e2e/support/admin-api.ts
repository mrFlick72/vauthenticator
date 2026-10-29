import {APIRequestContext, expect} from '@playwright/test';
import {AUTH_SERVER_URL} from '../playwright.config';

// the m2m "admin" client application seeded by the tenant-setup actuator endpoint
const ADMIN_CLIENT_ID = 'admin';
const ADMIN_CLIENT_SECRET = 'secret';

export type AdminAccount = {
    email: string;
    enabled: boolean;
    accountLocked: boolean;
    authorities: string[];
    mandatoryAction: 'NO_ACTION' | 'RESET_PASSWORD';
};

export type SignatureKey = {
    masterKey: string;
    kid: string;
    // 0 while the key signs new tokens, the expiration timestamp once rotated
    ttl: number;
};

// arranges and inspects data through the auth-server api, so a scenario drives only the screen it covers
export class AdminApi {
    private constructor(private readonly request: APIRequestContext, private readonly token: string) {
    }

    static async create(request: APIRequestContext) {
        const response = await request.post(`${AUTH_SERVER_URL}/oauth2/token`, {
            // without an explicit scope the token carries no scope claim at all
            form: {grant_type: 'client_credentials', scope: 'admin:full-access'},
            headers: {Authorization: basic(ADMIN_CLIENT_ID, ADMIN_CLIENT_SECRET)},
        });
        expect(response.ok()).toBe(true);
        return new AdminApi(request, (await response.json()).access_token);
    }

    private get headers() {
        return {Authorization: `Bearer ${this.token}`};
    }

    async signUp(email: string, password: string) {
        const response = await this.request.post(`${AUTH_SERVER_URL}/api/accounts`, {
            headers: this.headers,
            data: {email, password, firstName: 'E2E', lastName: 'Account'},
        });
        expect(response.status()).toBe(201);
    }

    async findAccount(email: string): Promise<AdminAccount> {
        const response = await this.request.get(`${AUTH_SERVER_URL}/api/admin/accounts/${email}/email`, {headers: this.headers});
        expect(response.ok()).toBe(true);
        return response.json();
    }

    async saveAccount(account: AdminAccount) {
        const response = await this.request.put(`${AUTH_SERVER_URL}/api/admin/accounts`, {headers: this.headers, data: account});
        expect(response.status()).toBe(204);
    }

    async template(kind: TemplateKind, type: string): Promise<string> {
        const response = await this.request.get(`${AUTH_SERVER_URL}/api/${kind}-template/${type}`, {headers: this.headers});
        expect(response.ok()).toBe(true);
        return (await response.json()).body;
    }

    async saveTemplate(kind: TemplateKind, type: string, body: string) {
        const response = await this.request.put(`${AUTH_SERVER_URL}/api/${kind}-template`, {
            headers: this.headers,
            data: {[kind === 'email' ? 'emailType' : 'smsType']: type, body},
        });
        expect(response.status()).toBe(204);
    }

    async signatureKeys(): Promise<SignatureKey[]> {
        const response = await this.request.get(`${AUTH_SERVER_URL}/api/keys`, {headers: this.headers});
        expect(response.ok()).toBe(true);
        return response.json();
    }

    async createSignatureKey() {
        const response = await this.request.post(`${AUTH_SERVER_URL}/api/keys`, {headers: this.headers});
        expect(response.status()).toBe(201);
    }
}

export type TemplateKind = 'email' | 'sms';

export function basic(username: string, password: string) {
    return `Basic ${Buffer.from(`${username}:${password}`).toString('base64')}`;
}

import {defineConfig, devices} from '@playwright/test';

export const ADMIN_STORAGE_STATE = '.auth/admin.json';
// the browser and the seeded client applications reach the auth-server through this host, see README.md
export const AUTH_SERVER_URL = 'http://local.api.vauthenticator.com:9090';
// must match the redirect uri registered for the seeded management ui client application
export const MANAGEMENT_UI_URL = 'http://local.management.vauthenticator.com:8085';

export default defineConfig({
    testDir: './tests',
    fullyParallel: false,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 1 : 0,
    reporter: process.env.CI ? [['list'], ['html', {open: 'never'}]] : 'list',
    use: {
        baseURL: MANAGEMENT_UI_URL,
        trace: 'retain-on-failure',
    },
    projects: [
        {
            name: 'setup',
            testMatch: /.*\.setup\.ts/,
        },
        {
            name: 'chromium',
            testIgnore: /keys\.spec\.ts/,
            use: {...devices['Desktop Chrome'], storageState: ADMIN_STORAGE_STATE},
            dependencies: ['setup'],
        },
        {
            // rotating and deleting signature keys is global state: run it alone, once everything else is done
            name: 'keys',
            testMatch: /keys\.spec\.ts/,
            use: {...devices['Desktop Chrome'], storageState: ADMIN_STORAGE_STATE},
            dependencies: ['chromium'],
        },
    ],
});

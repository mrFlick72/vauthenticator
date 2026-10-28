import {defineConfig, devices} from '@playwright/test';

export const ADMIN_STORAGE_STATE = '.auth/admin.json';
// the browser and the seeded client applications reach the auth-server through this host, see README.md
export const AUTH_SERVER_URL = 'http://local.api.vauthenticator.com:9090';

export default defineConfig({
    testDir: './tests',
    fullyParallel: false,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 1 : 0,
    reporter: process.env.CI ? [['list'], ['html', {open: 'never'}]] : 'list',
    use: {
        // must match the redirect uri registered for the seeded management ui client application
        baseURL: 'http://local.management.vauthenticator.com:8085',
        trace: 'retain-on-failure',
    },
    projects: [
        {
            name: 'setup',
            testMatch: /.*\.setup\.ts/,
        },
        {
            name: 'chromium',
            use: {...devices['Desktop Chrome'], storageState: ADMIN_STORAGE_STATE},
            dependencies: ['setup'],
        },
    ],
});

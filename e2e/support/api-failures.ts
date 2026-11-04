import {Page, Route} from '@playwright/test';
import {AUTH_SERVER_URL, MANAGEMENT_UI_URL} from '../playwright.config';

// the management ui calls the api cross-origin with credentials: a stubbed answer, and the preflight before it,
// must carry the cors headers or the browser rejects it before the ui ever sees the status
const corsHeaders = {
    'access-control-allow-origin': MANAGEMENT_UI_URL,
    'access-control-allow-credentials': 'true',
    'access-control-allow-methods': 'GET, POST, PUT, PATCH, DELETE',
    'access-control-allow-headers': 'authorization, content-type, accept',
};

/**
 * Answers every `method` call to the api `path` with `status` and `body`, without reaching the auth-server:
 * nothing a stubbed write would have changed is changed. Other calls to the same path go through.
 */
export async function failApiCall(page: Page, method: string, path: string | RegExp, status: number, body = '') {
    const matches = (url: URL) => url.origin === AUTH_SERVER_URL &&
        (typeof path === 'string' ? url.pathname === path : path.test(url.pathname));

    await page.route(matches, async (route: Route) => {
        const request = route.request();
        if (request.method() === 'OPTIONS' && request.headers()['access-control-request-method'] === method) {
            await route.fulfill({status: 204, headers: corsHeaders});
        } else if (request.method() === method) {
            await route.fulfill({status, body, headers: {...corsHeaders, 'content-type': 'text/plain'}});
        } else {
            await route.fallback();
        }
    });
}

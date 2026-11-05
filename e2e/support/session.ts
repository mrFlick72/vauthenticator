import {Page} from '@playwright/test';

// the management ui keeps its tokens in sessionStorage, which survives a reload of the same tab
export async function replaceAccessToken(page: Page, token: string) {
    await page.evaluate(value => window.sessionStorage.setItem('ACCESS_TOKEN', value), token);
}

export async function accessToken(page: Page) {
    return page.evaluate(() => window.sessionStorage.getItem('ACCESS_TOKEN'));
}

// an unsigned jwt the management ui reads as expired: the ui only looks at its exp claim before using it
export function expiredJwt() {
    const encode = (value: object) => Buffer.from(JSON.stringify(value)).toString('base64url');
    return `${encode({alg: 'none'})}.${encode({exp: Math.floor(Date.now() / 1000) - 60})}.`;
}

// records every top-level navigation request to the authorize endpoint, to prove a renewal happened: the
// endpoint redirects at once, so it never commits a navigation of its own that framenavigated would report
export function trackAuthorizeNavigations(page: Page) {
    const urls: string[] = [];
    page.on('request', request => {
        if (request.isNavigationRequest() && request.frame() === page.mainFrame() &&
            new URL(request.url()).pathname === '/oauth2/authorize') {
            urls.push(request.url());
        }
    });
    return urls;
}

import {expect, test} from '@playwright/test';
import {AdminHomePage, HOME_SECTIONS, HomeSection} from '../pages/admin-home-page';
import {LoginPage} from '../pages/login-page';
import {ADMIN_ACCOUNT, loginInFreshBrowser} from '../support/accounts';

const SECTION_PATHS: Record<HomeSection, RegExp> = {
    clientApplications: /\/secure\/admin\/client-applications\/list$/,
    roles: /\/secure\/admin\/roles$/,
    accounts: /\/secure\/admin\/accounts$/,
    passwordLifecycle: /\/secure\/admin\/password-lifecycle$/,
    keys: /\/secure\/admin\/keys$/,
    emailTemplates: /\/secure\/admin\/email-templates$/,
    smsTemplates: /\/secure\/admin\/sms-templates$/,
};

for (const section of Object.keys(HOME_SECTIONS) as HomeSection[]) {
    test(`the home menu opens the ${HOME_SECTIONS[section]} and the header leads back home`, async ({page}) => {
        const home = new AdminHomePage(page);
        await home.goto();

        await home.section(section).click();
        await expect(page).toHaveURL(SECTION_PATHS[section]);

        await home.header.homeLink.click();
        await expect(page).toHaveURL(/\/secure\/admin\/index$/);
        await expect(home.section(section)).toBeVisible();
    });
}

// logging out ends the auth-server session: done from a browser of its own, so the shared admin session
// the other scenarios run with stays valid
test('logout ends the session', async ({browser}) => {
    const {context, page} = await loginInFreshBrowser(browser, ADMIN_ACCOUNT);
    const home = new AdminHomePage(page);
    await expect(home.clientApplicationSection).toBeVisible();

    await home.header.logout();

    await home.goto();
    await expect(new LoginPage(page).loginButton).toBeVisible();
    await context.close();
});

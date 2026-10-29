import {expect, Locator, Page} from '@playwright/test';
import {AccountSearchPage} from './account-search-page';
import {ClientApplicationListPage} from './client-application-list-page';
import {AdminHeader} from './components/admin-header';
import {KeysPage} from './keys-page';
import {PasswordLifecyclePage} from './password-lifecycle-page';
import {RolesPage} from './roles-page';
import {EmailTemplatePage, SmsTemplatePage} from './template-pages';

export const HOME_SECTIONS = {
    clientApplications: 'Client Application Management Section',
    roles: 'Roles Management Section',
    accounts: 'Account Management Section',
    passwordLifecycle: 'Password Lifecycle Section',
    keys: 'Key Management Section',
    emailTemplates: 'EMail Template Management Section',
    smsTemplates: 'SMS Template Management Section',
} as const;

export type HomeSection = keyof typeof HOME_SECTIONS;

export class AdminHomePage {
    static readonly PATH = '/secure/admin/index';

    readonly header: AdminHeader;
    readonly clientApplicationSection: Locator;

    constructor(private readonly page: Page) {
        this.header = new AdminHeader(page);
        this.clientApplicationSection = this.section('clientApplications');
    }

    section(section: HomeSection) {
        return this.page.getByText(HOME_SECTIONS[section]);
    }

    async goto() {
        await this.page.goto(AdminHomePage.PATH);
    }

    private async open(section: HomeSection, path: RegExp) {
        await this.section(section).click();
        await expect(this.page).toHaveURL(path);
    }

    async openClientApplications() {
        await this.open('clientApplications', /\/client-applications\/list$/);
        const clientApplications = new ClientApplicationListPage(this.page);
        await clientApplications.showAllRows();
        return clientApplications;
    }

    async openRoles() {
        await this.open('roles', /\/roles$/);
        const roles = new RolesPage(this.page);
        await roles.table.showAllRows();
        return roles;
    }

    async openAccounts() {
        await this.open('accounts', /\/accounts$/);
        return new AccountSearchPage(this.page);
    }

    async openPasswordLifecycle() {
        await this.open('passwordLifecycle', /\/password-lifecycle$/);
        return new PasswordLifecyclePage(this.page);
    }

    async openKeys() {
        await this.open('keys', /\/keys$/);
        return new KeysPage(this.page);
    }

    async openEmailTemplates() {
        await this.open('emailTemplates', /\/email-templates$/);
        return new EmailTemplatePage(this.page);
    }

    async openSmsTemplates() {
        await this.open('smsTemplates', /\/sms-templates$/);
        return new SmsTemplatePage(this.page);
    }
}

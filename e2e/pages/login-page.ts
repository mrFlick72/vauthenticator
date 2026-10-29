import {Locator, Page} from '@playwright/test';

// auth-server login form, reached through the /oauth2/authorize redirect
export class LoginPage {
    readonly username: Locator;
    readonly password: Locator;
    readonly loginButton: Locator;

    constructor(private readonly page: Page) {
        this.username = page.locator('#username');
        this.password = page.locator('#password');
        this.loginButton = page.getByRole('button', {name: 'Login'});
    }

    async login(username: string, password: string) {
        await this.username.fill(username);
        await this.password.fill(password);
        await this.loginButton.click();
    }
}

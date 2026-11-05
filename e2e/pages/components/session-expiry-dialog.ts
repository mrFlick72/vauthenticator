import {Locator, Page} from '@playwright/test';

// the dialogs SessionManagement opens on every admin page: "Access expired" when the api rejects the access
// token (the session may or may not be alive), "Session expired" when the OP session iframe reports it ended
export class SessionExpiryDialogs {
    readonly any: Locator;
    readonly accessExpired: Locator;
    readonly sessionExpired: Locator;
    readonly continueButton: Locator;
    readonly logInAgainButton: Locator;
    // whichever dialog is open: both lead to the login form once the session has ended
    readonly proceedButton: Locator;

    constructor(page: Page) {
        this.any = page.getByRole('dialog', {name: /Access expired|Session expired/});
        this.accessExpired = page.getByRole('dialog', {name: 'Access expired'});
        this.sessionExpired = page.getByRole('dialog', {name: 'Session expired'});
        this.continueButton = this.accessExpired.getByRole('button', {name: 'Continue'});
        this.logInAgainButton = this.sessionExpired.getByRole('button', {name: 'Log in again'});
        this.proceedButton = this.any.getByRole('button', {name: /Continue|Log in again/});
    }
}

import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';
import {KeysPage} from '../pages/keys-page';
import {AdminApi} from '../support/admin-api';

// signature keys are global to the auth-server: this spec runs in the "keys" project, alone and after every
// other scenario, and only ever rotates or deletes keys it created itself, never a seeded one
test.describe.configure({mode: 'serial'});

let keys: KeysPage;
let adminApi: AdminApi;

test.beforeEach(async ({page, request}) => {
    adminApi = await AdminApi.create(request);
    const home = new AdminHomePage(page);
    await home.goto();
    keys = await home.openKeys();
});

async function aNewSignatureKey(page: { reload(): Promise<unknown> }) {
    const before = new Set((await adminApi.signatureKeys()).map(key => key.kid));
    await adminApi.createSignatureKey();
    const created = (await adminApi.signatureKeys()).find(key => !before.has(key.kid));
    expect(created).toBeDefined();
    await page.reload();
    await keys.table.showAllRows();
    return created!.kid;
}

// a key that signed the page's own token makes its next api call fail once deleted: reload, which goes
// through /oauth2/authorize again, before reading the table back
async function reloadKeys(page: { reload(): Promise<unknown> }) {
    await page.reload();
    await keys.table.showAllRows();
}

test('rotate a key', async ({page}) => {
    const kid = await aNewSignatureKey(page);
    const countBefore = (await adminApi.signatureKeys()).length;
    await expect(keys.expireIn(kid)).toHaveText('Never');

    expect(await keys.rotate(kid)).toBe(204);

    await reloadKeys(page);
    await expect(keys.expireIn(kid)).not.toHaveText('Never');
    const after = await adminApi.signatureKeys();
    expect(after.find(key => key.kid === kid)!.ttl).toBeGreaterThan(0);
    // the rotation replaces the key with a new one that signs from now on
    expect(after).toHaveLength(countBefore + 1);
});

test('cancelling the rotation leaves the key as it is', async ({page}) => {
    const kid = await aNewSignatureKey(page);

    await keys.cancelRotation(kid);

    expect((await adminApi.signatureKeys()).find(key => key.kid === kid)!.ttl).toBe(0);
    await reloadKeys(page);
    await expect(keys.expireIn(kid)).toHaveText('Never');
});

test('a rotated key cannot be rotated again', async ({page}) => {
    const kid = await aNewSignatureKey(page);
    expect(await keys.rotate(kid)).toBe(204);
    await reloadKeys(page);
    const expireIn = await keys.expireIn(kid).innerText();

    expect(await keys.rotateAlreadyRotated(kid)).toBe(false);

    await reloadKeys(page);
    await expect(keys.expireIn(kid)).toHaveText(expireIn);
});

test('delete a key', async ({page}) => {
    const kid = await aNewSignatureKey(page);

    expect(await keys.delete(kid)).toBe(204);

    expect((await adminApi.signatureKeys()).map(key => key.kid)).not.toContain(kid);
    await reloadKeys(page);
    await expect(keys.row(kid)).toHaveCount(0);
});

test('a rotated key cannot be deleted', async ({page}) => {
    const kid = await aNewSignatureKey(page);
    expect(await keys.rotate(kid)).toBe(204);
    await reloadKeys(page);

    // the same delete request is accepted for a key that was not rotated ("delete a key"), so this 400 is the
    // rotated key rule refusing it, not a malformed request
    expect(await keys.delete(kid)).toBe(400);

    expect((await adminApi.signatureKeys()).map(key => key.kid)).toContain(kid);
});

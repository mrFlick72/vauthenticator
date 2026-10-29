import {expect, test} from '@playwright/test';
import {AdminHomePage} from '../pages/admin-home-page';
import {Role, RolesPage} from '../pages/roles-page';
import {uniqueId} from '../support/unique';

let roles: RolesPage;

test.beforeEach(async ({page}) => {
    const home = new AdminHomePage(page);
    await home.goto();
    roles = await home.openRoles();
});

function aRole(): Role {
    const name = `ROLE_${uniqueId().toUpperCase().replace(/-/g, '_')}`;
    return {name, description: `E2E role ${name}`};
}

test('create a role', async ({page}) => {
    const role = aRole();

    await roles.create(role);

    await expect(roles.descriptionOf(role.name)).toHaveText(role.description);
    await page.reload();
    await roles.table.showAllRows();
    await expect(roles.descriptionOf(role.name)).toHaveText(role.description);
});

test('edit the description of a role, its name is read only', async ({page}) => {
    const role = aRole();
    await roles.create(role);

    await roles.openEditor(role.name);
    await expect(roles.name).toHaveValue(role.name);
    await expect(roles.name).toBeDisabled();
    await roles.description.fill(`${role.description} changed`);
    await roles.saveDialog();

    await page.reload();
    await roles.table.showAllRows();
    await expect(roles.descriptionOf(role.name)).toHaveText(`${role.description} changed`);
});

test('cancelling the role dialog creates nothing', async ({page}) => {
    const role = aRole();

    await roles.newRoleButton.click();
    await roles.name.fill(role.name);
    await roles.cancelDialog();

    await page.reload();
    await roles.table.showAllRows();
    await expect(roles.newRoleButton).toBeVisible();
    await expect(roles.row(role.name)).toHaveCount(0);
});

test('cancelling the deletion keeps the role', async () => {
    const role = aRole();
    await roles.create(role);

    await roles.cancelDelete(role.name);

    await expect(roles.row(role.name)).toBeVisible();
});

test('delete a role', async ({page}) => {
    const role = aRole();
    await roles.create(role);

    expect(await roles.delete(role.name)).toBe(204);

    await expect(roles.row(role.name)).toHaveCount(0);
    await page.reload();
    await roles.table.showAllRows();
    await expect(roles.newRoleButton).toBeVisible();
    await expect(roles.row(role.name)).toHaveCount(0);
});

test('a default role cannot be deleted', async ({page}) => {
    // 412 is the auth-server's answer for a role protected from deletion
    expect(await roles.delete('ROLE_USER')).toBe(412);

    await page.reload();
    await roles.table.showAllRows();
    await expect(roles.row('ROLE_USER')).toBeVisible();
});

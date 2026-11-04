import { getIdpBaseUrl } from "../../config/ConfigLoader";
import { ensureOk, errorMessageFor, jsonOf } from "../../utils/ApiError";

export const rolesMessageFor = (error: unknown) => errorMessageFor(error, "roles", "role")

export interface Role {
    name: string,
    description: string
}

export async function findAllRoles(): Promise<Role[]> {
    const baseUrl = await getIdpBaseUrl()

    let response = await fetch(`${baseUrl}/api/roles`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
        });
    return jsonOf<Role[]>(response)
}

export async function deleteRoleFor(roleId: string): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/roles/${roleId}`,
        {
            method: "DELETE",
            headers: {
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        })
    await ensureOk(response)
}

export async function saveRoleFor(role: Role): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/roles`,
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify(role)
        })
    await ensureOk(response)
}

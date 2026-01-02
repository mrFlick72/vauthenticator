import { getIdpBaseUrl } from "../../config/ConfigLoader";

export interface Role {
    name: string,
    description: string
}

export async function findAllRoles() {
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
    return await response.json() as Role[];
}

export async function deleteRoleFor(roleId: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/roles/${roleId}`,
        {
            method: "DELETE",
            headers: {
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        })
}

export async function saveRoleFor(role: Role) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/roles`,
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
}

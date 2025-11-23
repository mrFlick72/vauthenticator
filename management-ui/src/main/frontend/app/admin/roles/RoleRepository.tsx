export interface Role {
    name: string,
    description: string
}

export async function findAllRoles() {
    let response = await fetch("/secure/api/roles",
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });
    return await response.json() as Role[];
}

export function deleteRoleFor(roleId: string) {
    return fetch(`/secure/api/roles/${roleId}`,
        {
            method: "DELETE",
            credentials: 'same-origin'
        })
}

export function saveRoleFor(role: Role) {
    return fetch("/secure/api/roles",
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(role),
            credentials: 'same-origin'
        })
}

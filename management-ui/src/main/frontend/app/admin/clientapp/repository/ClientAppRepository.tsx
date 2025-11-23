import {ClientApplicationDetails, ClientApplicationInList} from "./ClientAppApiTypes";

export type RandomSecret = {
    pwd: string
}

export async function newClientApplicationRandomSecret(): Promise<RandomSecret> {
    let response = await fetch("/secure/api/password",
        {
            method: "POST",
            credentials: 'same-origin'
        });
    return await response.json() as RandomSecret
}

export async function findAllClientApplications(): Promise<ClientApplicationInList[]> {
    let response = await fetch("/secure/api/client-applications",
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });
    return await response.json() as ClientApplicationInList[]
}

export async function findClientApplicationFor(clientAppId: string): Promise<ClientApplicationDetails> {
    let response = await fetch(`/secure/api/client-applications/${clientAppId}`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });
    return await response.json() as ClientApplicationDetails
}

export function saveClientApplicationFor(clientAppId: string, clientApp: ClientApplicationDetails) {
    return fetch(`/secure/api/client-applications/${clientAppId}`,
        {
            method: "PUT",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(clientApp),
            credentials: 'same-origin'
        })
}

export function resetSecretFor(clientAppId: string, secret: string) {
    return fetch(`/secure/api/client-applications/${clientAppId}/client-secret`,
        {
            method: "PATCH",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({"secret": secret}),
            credentials: 'same-origin'
        })
}

export function deleteClientApplicationFor(clientAppId: string) {
    return fetch(`/secure/api/client-applications/${clientAppId}`,
        {
            method: "DELETE",
            credentials: 'same-origin'
        })
}
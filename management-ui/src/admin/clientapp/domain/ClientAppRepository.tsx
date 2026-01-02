import { getIdpBaseUrl } from "../../../config/ConfigLoader";
import { ClientApplicationDetails, ClientApplicationInList } from "./ClientAppApiTypes";

export type RandomSecret = {
    pwd: string
}

export async function newClientApplicationRandomSecret(): Promise<RandomSecret> {
    const baseUrl = await getIdpBaseUrl()

    let response = await fetch(`${baseUrl}/api/password`,
        {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        });
    return await response.json() as RandomSecret
}

export async function findAllClientApplications(): Promise<ClientApplicationInList[]> {
    const baseUrl = await getIdpBaseUrl()

    let response = await fetch(`${baseUrl}/api/client-applications`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        });
    return await response.json() as ClientApplicationInList[]
}

export async function findClientApplicationFor(clientAppId: string): Promise<ClientApplicationDetails> {
    const baseUrl = await getIdpBaseUrl()

    let response = await fetch(`${baseUrl}/api/client-applications/${clientAppId}`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        });
    return await response.json() as ClientApplicationDetails
}

export async function saveClientApplicationFor(clientAppId: string, clientApp: ClientApplicationDetails) {
    const baseUrl = await getIdpBaseUrl()
    return fetch(`${baseUrl}/api/client-applications/${clientAppId}`,
        {
            method: "PUT",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify(clientApp),
        })
}

export async function resetSecretFor(clientAppId: string, secret: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/client-applications/${clientAppId}/client-secret`,
        {
            method: "PATCH",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify({ "secret": secret }),
        })
}

export async function deleteClientApplicationFor(clientAppId: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/client-applications/${clientAppId}`,
        {
            method: "DELETE",
            headers: {
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`
            },
            mode: "cors",
            credentials: 'include'
        })
}
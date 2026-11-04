import { getIdpBaseUrl } from "../../../config/ConfigLoader";
import { ClientApplicationDetails, ClientApplicationInList } from "./ClientAppApiTypes";
import { ensureOk, errorMessageFor, jsonOf } from "../../../utils/ApiError";

export const clientAppsMessageFor = (error: unknown) =>
    errorMessageFor(error, "client applications", "client application")

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
    return jsonOf<RandomSecret>(response)
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
    return jsonOf<ClientApplicationInList[]>(response)
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
    return jsonOf<ClientApplicationDetails>(response)
}

export async function saveClientApplicationFor(clientAppId: string , clientApp: ClientApplicationDetails): Promise<void> {
    const baseUrl = await getIdpBaseUrl()
    const response = await fetch(`${baseUrl}/api/client-applications/${clientAppId}`,
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
    await ensureOk(response)
}

export async function resetSecretFor(clientAppId: string, secret: string): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/client-applications/${clientAppId}/client-secret`,
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
    await ensureOk(response)
}

export async function deleteClientApplicationFor(clientAppId: string): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/client-applications/${clientAppId}`,
        {
            method: "DELETE",
            headers: {
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`
            },
            mode: "cors",
            credentials: 'include'
        })
    await ensureOk(response)
}
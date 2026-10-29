import { getIdpBaseUrl } from "../../config/ConfigLoader"

const default_rotation_ttl = 604800

export interface VAuthenticatorKey {
    masterKey: string
    kid: string
    ttl: number
    expireIn: string
}

export async function findAllKeys() {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/keys`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        })
}

export async function deleteKeyFor(kid: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/keys`,
        {
            method: "DELETE",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            // a key_ttl of 0 deletes the key now; the api requires the field
            body: JSON.stringify({kid: kid, "key_purpose": "SIGNATURE", "key_ttl": 0})
        })
}

export async function rotateKeyFor(kid: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/keys/rotate`,
        {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify({kid: kid, "key_ttl": default_rotation_ttl})
        })
}
const default_rotation_ttl = 604800

export interface VAuthenticatorKey {
    masterKey: string
    kid: string
    ttl: number
    expireIn: string
}

export function findAllKeys() {
    return fetch("/secure/api/keys",
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        })
}

export function deleteKeyFor(kid: string) {
    return fetch("/secure/api/keys",
        {
            method: "DELETE",
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin',
            body: JSON.stringify({kid: kid, "key_purpose": "SIGNATURE"})
        })
}

export function rotateKeyFor(kid: string) {
    return fetch("/secure/api/keys/rotate",
        {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin',
            body: JSON.stringify({kid: kid, "key_ttl": default_rotation_ttl})
        })
}
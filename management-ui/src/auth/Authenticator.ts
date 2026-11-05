import SHA256 from 'crypto-js/sha256';
import Base64 from 'crypto-js/enc-base64';
import {applicationConfigLoader} from "../config/ConfigLoader";

export type TokenResponse = {
    id_token: string
    access_token: string
    scope: string
    token_type: string
    expires_in: number
}

const randomDataString = (): string => {
    let possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

    let text = ""
    for (let i = 0; i < 40; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    return text
}

type AuthorizeType = "login" | "none"

export const getAuthorizeUrlFor = async (type: AuthorizeType, storeCodeVerifier = true): Promise<string> => {
    const nonce = randomDataString()
    const state = randomDataString()
    const codeVerifier = randomDataString()

    const base64 = Base64.stringify(SHA256(codeVerifier));
    const codeChallenge = base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

    let oauth2Config = await applicationConfigLoader()
    if (storeCodeVerifier) {
        window.sessionStorage.setItem("codeVerifier", codeVerifier);
    }
    let promptType = type;
    return `${oauth2Config.idpBaseUrl}/oauth2/authorize?response_type=code&client_id=${oauth2Config.clientApplicationId}&redirect_uri=${oauth2Config.redirectUri}&scope=${oauth2Config.scope}&state=${state}&nonce=${nonce}&code_challenge=${codeChallenge}&code_challenge_method=S256&prompt=${promptType}`
}

const isVAuthenticatorLoginUrl = (url: string, idpBaseUrl: string) => {
    if (!url) {
        return false
    }

    const idpUrl = new URL(idpBaseUrl)
    const redirectUrl = new URL(url, idpUrl.origin)

    return redirectUrl.origin === idpUrl.origin && redirectUrl.pathname === "/login"
}

/**
 * The check every admin page runs on mount. Missing, malformed or expired tokens are renewed from the session at
 * once, without asking: a page that has just loaded holds nothing the admin could lose. When the session has
 * ended too, the authorization server shows its login form.
 *
 * It reads the access token's own expiry instead of asking the authorization server, so a server that is down
 * cannot send it into a redirect loop. A token rejected for another reason, such as a revoked one, still meets
 * the "Access expired" dialog on its first api call.
 */
export const isAuthenticated = async () => {
    if (!hasUsableTokens()) {
        await renewAccess()
    }
}

// renewing a little before the expiry keeps a call made right after the check from being rejected
const EXPIRY_MARGIN_SECONDS = 30

const hasUsableTokens = (): boolean => {
    const idToken = window.sessionStorage.getItem("ID_TOKEN");
    const accessToken = window.sessionStorage.getItem("ACCESS_TOKEN");
    if (!idToken || !accessToken) {
        return false
    }
    const expiresAt = expiryOf(accessToken)
    return expiresAt !== null && expiresAt - EXPIRY_MARGIN_SECONDS > Date.now() / 1000
}

// the exp claim of a jwt, in seconds; null when the token is not a readable jwt
const expiryOf = (jwt: string): number | null => {
    try {
        const payload = jwt.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")
        const exp = JSON.parse(window.atob(payload)).exp
        return typeof exp === "number" ? exp : null
    } catch {
        return null
    }
}

/**
 * Expired Access: fresh tokens from the living session, without credentials, then back to this page.
 */
export const renewAccess = async () => {
    window.sessionStorage.setItem("returnTo", window.location.href);
    window.location.href = await getAuthorizeUrlFor("none")
}

/**
 * Expired Session: the admin logs in again with their credentials, then comes back to this page.
 */
export const logInAgain = async () => {
    window.sessionStorage.setItem("returnTo", window.location.href);
    window.location.href = await getAuthorizeUrlFor("login")
}

/**
 * The errors an authorization request with prompt=none answers when only an interactive login can continue.
 */
export const isInteractionRequired = (error: string | null) =>
    error === "login_required" || error === "interaction_required" ||
    error === "consent_required" || error === "account_selection_required"

export const authenticate = async (code: string) => {
    let oauth2Config = await applicationConfigLoader()

    let tokenUrl = `${oauth2Config.idpBaseUrl}/oauth2/token`
    const codeVerifier = window.sessionStorage.getItem("codeVerifier")
    const requestBody = `grant_type=authorization_code&client_id=${oauth2Config.clientApplicationId}&code=${code}&redirect_uri=${oauth2Config.redirectUri}&code_verifier=${codeVerifier}`
    await fetch(tokenUrl, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "application/json",
        },
        body: requestBody,
    })
        .then((response) => response.json())
        .then((data) => {
            let tokenResponse = data as TokenResponse

            if (tokenResponse.id_token) {
                window.sessionStorage.setItem("ID_TOKEN", tokenResponse.id_token);
            }
            if (tokenResponse.access_token) {
                window.sessionStorage.setItem("ACCESS_TOKEN", tokenResponse.access_token);
            }
            window.sessionStorage.removeItem("codeVerifier")
        })
}

export const invalidateSession = () => {
    window.sessionStorage.removeItem("ID_TOKEN");
    window.sessionStorage.removeItem("ACCESS_TOKEN");
    window.sessionStorage.removeItem("SESSION_STATE");
}

export const endOfSession = async () => {
    const oauth2Config = await applicationConfigLoader()
    const returnTo = document.referrer
    const idTokenHint = window.sessionStorage.getItem("ID_TOKEN")

    invalidateSession();

    window.location.replace(encodeURI(`${oauth2Config.idpBaseUrl}/connect/logout?id_token_hint=${idTokenHint}&post_logout_redirect_uri=${returnTo}`))
}

export const checkOfSession = async (): Promise<boolean> => {
    try {
        let oauth2Config = await applicationConfigLoader()
        let authorizationUrl = await getAuthorizeUrlFor("none", false)
        let response = await fetch(authorizationUrl, {
            method: "GET",
            credentials: "include",
            redirect: "follow",
        })

        return response.ok && !isVAuthenticatorLoginUrl(response.url, oauth2Config.idpBaseUrl)
    } catch (e) {
        console.error("Session check failed", e)
        return false
    }
}


import CryptoJS from 'crypto-js';
import {applicationConfigLoader} from "../config/ConfigLoader";

const ACCESS_TOKEN_ID = "ACCESS_TOKEN";
const ID_TOKEN_ID = "ID_TOKEN";

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

export const isAuthenticated = async () => {
    const nonce = randomDataString()
    const state = randomDataString()
    const codeVerifier = randomDataString()

    const base64 = CryptoJS.enc.Base64.stringify(CryptoJS.SHA256(codeVerifier));
    const codeChallenge = base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

    let oauth2Config = await applicationConfigLoader()
    window.sessionStorage.setItem("returnTo", window.location.href);
    if (!await hasValidTokens()) {
        window.location.href = `${oauth2Config.idpBaseUrl}/oauth2/authorize?response_type=code&client_id=${oauth2Config.clientApplicationId}&redirect_uri=${oauth2Config.redirectUri}&scope=${oauth2Config.scope}&state=${state}&nonce=${nonce}&code_challenge=${codeChallenge}&code_challenge_method=S256`
        window.sessionStorage.setItem("codeVerifier", codeVerifier);
    }
}

const hasValidTokens = async () => {
    const oauth2Config = await applicationConfigLoader()
    const idToken = window.sessionStorage.getItem(ID_TOKEN_ID);
    const accessToken = window.sessionStorage.getItem(ACCESS_TOKEN_ID);

    if (idToken && accessToken) {
        let response = await fetch(`${oauth2Config.idpBaseUrl}/userinfo`, {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + accessToken,
                "Accept": "application/json",
            },
            mode: "cors",
            credentials: 'include'
        })
        console.log(response)
        return response.status != 403
    } else {
        return false
    }
}

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
                window.sessionStorage.setItem(ID_TOKEN_ID, tokenResponse.id_token);
            }
            if (tokenResponse.access_token) {
                window.sessionStorage.setItem(ACCESS_TOKEN_ID, tokenResponse.access_token);
            }
            window.sessionStorage.removeItem("codeVerifier")
        })
}

export const endOfSession = async () => {
    const oauth2Config = await applicationConfigLoader()
    const returnTo = document.referrer
    const idTokenHint = window.sessionStorage.getItem(ID_TOKEN_ID)

    window.sessionStorage.removeItem(ID_TOKEN_ID);
    window.sessionStorage.removeItem(ACCESS_TOKEN_ID);

    window.location.href = `${oauth2Config.idpBaseUrl}/connect/logout?id_token_hint=${idTokenHint}&post_logout_redirect_uri=${returnTo}`
}

export const authenticationChecker = () => {
    applicationConfigLoader()
        .then(config => {
            setInterval(() => {
                isAuthenticated().then()
            }, config.authenticationCheckInterval * 1000)
        })

}

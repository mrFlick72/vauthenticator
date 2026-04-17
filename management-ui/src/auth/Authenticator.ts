import CryptoJS from 'crypto-js';
import {createRemoteJWKSet, jwtVerify, JWTVerifyOptions} from 'jose';
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

type OpenIdConfiguration = {
    issuer: string
    jwks_uri: string
}

type TokenValidationOptions = JWTVerifyOptions & {
    jwksUri: string
}

let openIdConfigurationCache: OpenIdConfiguration | undefined
let signingKeySetCache: ReturnType<typeof createRemoteJWKSet> | undefined
let signingKeySetUrlCache: string | undefined

const loadOpenIdConfiguration = async (idpBaseUrl: string): Promise<OpenIdConfiguration> => {
    if (!openIdConfigurationCache) {
        const response = await fetch(`${idpBaseUrl}/.well-known/openid-configuration`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
            },
            mode: "cors",
            credentials: 'include'
        })

        if (!response.ok) {
            throw new Error(`Unable to load openid configuration: ${response.status}`)
        }

        openIdConfigurationCache = await response.json() as OpenIdConfiguration
    }

    return openIdConfigurationCache
}

const loadSigningKeySet = (jwksUri: string) => {
    console.log(`Loading signing key set from ${jwksUri}`)
    if (!signingKeySetCache || signingKeySetUrlCache !== jwksUri) {
        signingKeySetCache = createRemoteJWKSet(new URL(jwksUri))
        signingKeySetUrlCache = jwksUri
    }

    return signingKeySetCache
}

const validateJwtToken = async (token: string, options: TokenValidationOptions): Promise<boolean> => {
    try {
        const signingKeySet = loadSigningKeySet(options.jwksUri as string)

        await jwtVerify(token, signingKeySet, {
            issuer: options.issuer,
            clockTolerance: 30,
        })

        return true
    } catch (error) {
        console.error("Token validation failed", error)
        return false
    }
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

    if (!idToken || !accessToken) {
        return false
    }

    const openIdConfiguration = await loadOpenIdConfiguration(oauth2Config.idpBaseUrl)
    const [idTokenIsValid, accessTokenIsValid] = await Promise.all([
        validateJwtToken(idToken, {
            issuer: openIdConfiguration.issuer,
            jwksUri: openIdConfiguration.jwks_uri,
        }),
        validateJwtToken(accessToken, {
            issuer: openIdConfiguration.issuer,
            jwksUri: openIdConfiguration.jwks_uri,
        }),
    ])

    return idTokenIsValid && accessTokenIsValid
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

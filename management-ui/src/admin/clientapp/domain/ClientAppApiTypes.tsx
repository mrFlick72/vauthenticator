
export type ClientApplicationDetails = {
    clientAppName: string
    secret: string
    confidential: boolean
    withPkce: boolean
    storePassword: boolean
    scopes: string[]
    authorizedGrantTypes: string[]
    webServerRedirectUri: string
    accessTokenValidity: string
    refreshTokenValidity: string
    postLogoutRedirectUri: string
    logoutUri: string,
    allowedOrigins: string[]
}

export type ClientApplicationInList = {
    clientAppId: string
    clientAppName: string
    scopes: string[]
    authorizedGrantTypes: string[]
}

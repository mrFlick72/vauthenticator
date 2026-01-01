type ApplicationConfig = {
    scope: string,
    redirectUri: string,
    clientApplicationId: string,
    idpBaseUrl: string
    authenticationCheckInterval: number

}
export const applicationConfigLoader = async () => {
    const configData = {
        scope: "openid email profile",
        redirectUri: process.env.REDIRECT_URI,
        clientApplicationId: process.env.CLIENT_APPLICATION_ID,
        idpBaseUrl: process.env.IDP_BASE_URL,
        authenticationCheckInterval: Number(process.env.AUTHENTICATION_CHECK_INTERVAL),

    };
    return configData as ApplicationConfig
}

export async function getIdpBaseUrl() {
    const appConfig = await applicationConfigLoader()
    return appConfig.idpBaseUrl;
}

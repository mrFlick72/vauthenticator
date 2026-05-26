type ApplicationConfig = {
    scope: string,
    redirectUri: string,
    clientApplicationId: string,
    idpBaseUrl: string
    apiBaseUrl: string
    authenticationCheckInterval: number

}

type ConfigManagerResponse = {
    redirectUri: string,
    clientApplicationId: string,
    idpBaseUrl: string
    apiBaseUrl: string
    authenticationCheckInterval: string | number
}

const appConfigStorageKey = "appConfig"
const defaultScope = "openid email profile"
const configManagerEndpoint = "/api/config"
let applicationConfigRequest: Promise<ApplicationConfig> | null = null

const isLogoutRequest = () => {
    const pathname = window.location.pathname
    return pathname.endsWith("/logout") || pathname.endsWith("/logout.html")
}

const toApplicationConfig = (configData: ConfigManagerResponse): ApplicationConfig => ({
    scope: defaultScope,
    redirectUri: configData.redirectUri,
    clientApplicationId: configData.clientApplicationId,
    idpBaseUrl: configData.idpBaseUrl,
    apiBaseUrl: configData.apiBaseUrl,
    authenticationCheckInterval: Number(configData.authenticationCheckInterval),
})

const loadApplicationConfigFromCache = (): ApplicationConfig | null => {
    const cachedConfig = window.sessionStorage.getItem(appConfigStorageKey)

    if (!cachedConfig) {
        return null
    }

    try {
        return toApplicationConfig(JSON.parse(cachedConfig) as ConfigManagerResponse)
    } catch (e) {
        window.sessionStorage.removeItem(appConfigStorageKey)
        return null
    }
}

const loadApplicationConfigFromConfigManager = async (): Promise<ApplicationConfig> => {
    const response = await fetch(configManagerEndpoint, {
        method: "GET",
        headers: {
            "Accept": "application/json",
        },
        mode: "cors",
    })

    if (!response.ok) {
        throw new Error(`Unable to load application configuration: ${response.status}`)
    }

    return toApplicationConfig(await response.json() as ConfigManagerResponse)
}

const cacheApplicationConfig = (configData: ApplicationConfig) => {
    window.sessionStorage.setItem(appConfigStorageKey, JSON.stringify(configData))
}

export const clearApplicationConfigCache = () => {
    window.sessionStorage.removeItem(appConfigStorageKey)
    applicationConfigRequest = null
}

export const applicationConfigLoader = async () => {
    const logoutRequest = isLogoutRequest()
    const cachedConfig = loadApplicationConfigFromCache()

    if (cachedConfig) {
        if (logoutRequest) {
            clearApplicationConfigCache()
        }
        return cachedConfig
    }

    if (!applicationConfigRequest) {
        applicationConfigRequest = loadApplicationConfigFromConfigManager()
            .catch((e) => {
                applicationConfigRequest = null
                throw e
            })
    }

    const configData = await applicationConfigRequest

    if (!logoutRequest) {
        cacheApplicationConfig(configData)
    } else {
        clearApplicationConfigCache()
    }

    return configData
}

export async function getIdpBaseUrl() {
    const appConfig = await applicationConfigLoader()
    return appConfig.idpBaseUrl;
}

export async function getApiBaseUrl() {
    const appConfig = await applicationConfigLoader()
    return appConfig.apiBaseUrl;
}

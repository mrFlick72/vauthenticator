export enum AuthorizedGrantType {
    authorization_code = "authorization_code",
    refresh_token = "refresh_token",
    client_credentials = "client_credentials"
}

export type ClientAppAuthorizedGrantType = {
    authorization_code: boolean
    refresh_token: boolean
    client_credentials: boolean
}

export function authorizedGrantTypesParam(input: ClientAppAuthorizedGrantType): string[] {
    return Object.entries(input)
        .filter((value: [string, boolean]) => value[1])
        .map((value: [string, boolean]) => value[0])
}

export const authorizedGrantTypesRegistry = (authorizedGrantTypes: AuthorizedGrantType[]): ClientAppAuthorizedGrantType => {
    let registry: ClientAppAuthorizedGrantType = {
        authorization_code: false,
        refresh_token: false,
        client_credentials: false
    }

    if (authorizedGrantTypes) {
        authorizedGrantTypes.forEach(authorizedGrantType => {
            registry[authorizedGrantType] = true;
        })
    }

    return registry;
}
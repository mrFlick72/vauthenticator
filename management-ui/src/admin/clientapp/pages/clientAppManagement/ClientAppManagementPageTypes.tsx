import {SelectOption} from "../../../../components/FormSelect";
import { ClientAppAuthorizedGrantType } from "./AuthorizedGrantTypes";

export type ClientAppManagementPageTypes = {
    clientApplicationId: string | undefined
    clientAppName: string
    secret: string
    applicationType: SelectOption
    scopes: SelectOption[]
    authorizedGrantTypes: ClientAppAuthorizedGrantType
    webServerRedirectUri: string
    allowedOrigins: string
    withPkce: boolean
    accessTokenValidity: string
    refreshTokenValidity: string
    postLogoutRedirectUri: string
    logoutUri: string
}
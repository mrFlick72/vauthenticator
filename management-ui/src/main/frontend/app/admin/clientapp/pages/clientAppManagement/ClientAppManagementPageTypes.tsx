import {SelectOption} from "../../../../component/FormSelect";
import { ClientAppAuthorizedGrantType } from "../clientAppManagement/AuthorizedGrantTypes";

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
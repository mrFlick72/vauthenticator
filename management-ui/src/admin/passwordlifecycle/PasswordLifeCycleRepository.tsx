import {getIdpBaseUrl} from "../../config/ConfigLoader";

export enum PasswordLifeCycleAction {
    PASSWORD_RESET = "PASSWORD_RESET",
    ACCOUNT_LOCK = "ACCOUNT_LOCK"
}

export const passwordLifeCycleActionOptions = [
    {value: PasswordLifeCycleAction.PASSWORD_RESET, label: "PASSWORD_RESET (recurring)"},
    {value: PasswordLifeCycleAction.ACCOUNT_LOCK, label: "ACCOUNT_LOCK (one-shot)"},
]

export type PasswordLifeCycleRule = {
    action: PasswordLifeCycleAction,
    intervalSeconds: number,
    creationDate: string,
    lastEvaluationDate: string | null,
    nextEvaluationDate: string
}

export type PasswordLifeCycleBulkResult = {
    matchedAccounts: number
}

const authorizationHeader = () => `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`

const accountRulesUrl = async (userName: string) =>
    `${await getIdpBaseUrl()}/api/admin/accounts/${encodeURIComponent(userName)}/password/lifecycle`

const bulkRulesUrl = async (action: PasswordLifeCycleAction) =>
    `${await getIdpBaseUrl()}/api/admin/accounts/password/lifecycle/${action}/bulk`

export async function findRulesFor(userName: string) {
    return fetch(await accountRulesUrl(userName),
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include'
        })
}

export async function saveRuleFor(userName: string, action: PasswordLifeCycleAction, intervalSeconds: number) {
    return fetch(`${await accountRulesUrl(userName)}/${action}`,
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify({intervalSeconds: intervalSeconds})
        })
}

export async function removeRuleFor(userName: string, action: PasswordLifeCycleAction) {
    return fetch(`${await accountRulesUrl(userName)}/${action}`,
        {
            method: "DELETE",
            headers: {
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include'
        })
}

export async function saveRuleForAccountPattern(accountPattern: string,
                                                action: PasswordLifeCycleAction,
                                                intervalSeconds: number) {
    return fetch(await bulkRulesUrl(action),
        {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify({accountPattern: accountPattern, intervalSeconds: intervalSeconds})
        })
}

export async function removeRuleForAccountPattern(accountPattern: string, action: PasswordLifeCycleAction) {
    return fetch(`${await bulkRulesUrl(action)}?accountPattern=${encodeURIComponent(accountPattern)}`,
        {
            method: "DELETE",
            headers: {
                'Accept': 'application/json',
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include'
        })
}

/**
 * Turns a failed response into a message for the admin, using the backend's own message for a 400.
 */
export async function errorMessageFor(response: Response): Promise<string> {
    switch (response.status) {
        case 400: {
            const detail = await response.text()
            return detail ? `The request is not valid: ${detail}` : "The request is not valid"
        }
        case 401:
            return "Your session has expired, please log in again"
        case 403:
            return "You are not allowed to manage password lifecycle rules"
        case 404:
            return "The account does not exist"
        default:
            return `Unexpected error (HTTP ${response.status})`
    }
}

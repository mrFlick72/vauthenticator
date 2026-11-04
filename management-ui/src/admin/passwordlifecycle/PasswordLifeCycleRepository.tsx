import {getIdpBaseUrl} from "../../config/ConfigLoader";
import {ensureOk, errorMessageFor, jsonOf} from "../../utils/ApiError";

// a 404 here means the account itself does not exist
export const passwordLifeCycleMessageFor = (error: unknown) =>
    errorMessageFor(error, "password lifecycle rules", "account")

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

export async function findRulesFor(userName: string): Promise<PasswordLifeCycleRule[]> {
    const response = await fetch(await accountRulesUrl(userName),
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include'
        })
    return jsonOf<PasswordLifeCycleRule[]>(response)
}

export async function saveRuleFor(userName: string, action: PasswordLifeCycleAction, intervalSeconds: number): Promise<void> {
    const response = await fetch(`${await accountRulesUrl(userName)}/${action}`,
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
    await ensureOk(response)
}

export async function removeRuleFor(userName: string, action: PasswordLifeCycleAction): Promise<void> {
    const response = await fetch(`${await accountRulesUrl(userName)}/${action}`,
        {
            method: "DELETE",
            headers: {
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include'
        })
    await ensureOk(response)
}

export async function saveRuleForAccountPattern(accountPattern: string,
                                                action: PasswordLifeCycleAction,
                                                intervalSeconds: number): Promise<PasswordLifeCycleBulkResult> {
    const response = await fetch(await bulkRulesUrl(action),
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
    return jsonOf<PasswordLifeCycleBulkResult>(response)
}

export async function removeRuleForAccountPattern(accountPattern: string, action: PasswordLifeCycleAction): Promise<PasswordLifeCycleBulkResult> {
    const response = await fetch(`${await bulkRulesUrl(action)}?accountPattern=${encodeURIComponent(accountPattern)}`,
        {
            method: "DELETE",
            headers: {
                'Accept': 'application/json',
                'Authorization': authorizationHeader(),
            },
            mode: "cors",
            credentials: 'include'
        })
    return jsonOf<PasswordLifeCycleBulkResult>(response)
}

import { getIdpBaseUrl } from "../../config/ConfigLoader";
import { ensureOk, errorMessageFor } from "../../utils/ApiError";

export const accountsMessageFor = (error: unknown) => errorMessageFor(error, "accounts", "account")

export enum MandatoryAction {
    NO_ACTION = "NO_ACTION",
    RESET_PASSWORD = "RESET_PASSWORD"


}
export function convertToMandatoryAction(str: string): MandatoryAction {
    return MandatoryAction[str as keyof typeof MandatoryAction];
}
type Account = {
    email: string,
    enabled: boolean,
    accountLocked: boolean,
    mandatoryAction: MandatoryAction,
    authorities: string[],
}

export async function findAccountFor(email: string): Promise<Account | undefined> {
    const baseUrl = await getIdpBaseUrl()

    let response = await fetch(`${baseUrl}/api/admin/accounts/${email}/email`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        });
    // an unknown account comes back as 200 with an empty body
    let body = await (await ensureOk(response)).text();
    return body ? JSON.parse(body) as Account : undefined
}

export async function saveAccountFor(account: Account): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/admin/accounts`,
        {
            method: "PUT",  
            headers: {
                'Content-Type': 'application/json'
                ,
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify(account),
        })
    await ensureOk(response)

}

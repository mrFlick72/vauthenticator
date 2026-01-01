import { getIdpBaseUrl } from "../../config/ConfigLoader";

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

export async function findAccountFor(email: string) {
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
    let body = await response.json();
    return Promise.resolve(body as Account)
}

// todo change result type
export async function saveAccountFor(account: Account)  {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/admin/accounts`,
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

}

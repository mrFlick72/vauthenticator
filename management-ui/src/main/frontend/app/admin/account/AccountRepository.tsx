export enum MandatoryAction {
    NO_ACTION = "NO_ACTION",
    RESET_PASSWORD = "RESET_PASSWORD"


}
export function convertToMandatoryAction(str: string): MandatoryAction {
    return MandatoryAction[str as keyof typeof MandatoryAction];
}
type Account =  {
    email: string,
    enabled: boolean,
    accountLocked: boolean,
    mandatoryAction: MandatoryAction,
    authorities: string[],
}

export async function findAccountFor(email: string) {
    let response = await fetch(`/secure/api/admin/accounts/${email}/email`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });
    let body = await response.json();
    return Promise.resolve(body as Account)
}

export function saveAccountFor(account: Account) {
    return fetch(`/secure/api/admin/accounts`,
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(account),
            credentials: 'same-origin'
        })
}

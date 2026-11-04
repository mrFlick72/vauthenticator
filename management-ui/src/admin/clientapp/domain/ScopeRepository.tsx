import { getIdpBaseUrl } from "../../../config/ConfigLoader";
import { jsonOf } from "../../../utils/ApiError";

export async function findAllScopes(): Promise<string[]> {
        const baseUrl = await getIdpBaseUrl()

    let response = await fetch(`${baseUrl}/.well-known/openid-configuration`,
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            mode: "cors",
            credentials: 'include'
        });
    const response_body = await jsonOf<{ scopes_supported: string[] }>(response)
    return response_body.scopes_supported
}

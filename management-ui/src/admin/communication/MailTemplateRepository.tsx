import { getIdpBaseUrl } from "../../config/ConfigLoader"

interface MailTemplateRequest {
    emailType: string
    body?: string
}

export async function saveMailTemplateFor(mailTemplate: MailTemplateRequest) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/email-template`,
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify(mailTemplate),
        })
}

export async function getMailTemplateFor(mailType: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/email-template/${mailType}`,
        {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        }).then(r => r.json())
}

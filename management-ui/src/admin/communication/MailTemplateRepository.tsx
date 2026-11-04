import { getIdpBaseUrl } from "../../config/ConfigLoader"
import { ApiSubject, ensureOk, jsonOf } from "../../utils/ApiError"

export const EMAIL_TEMPLATES: ApiSubject = { managed: "email templates", missing: "email template" }

interface MailTemplate {
    body: string
}

interface MailTemplateRequest {
    emailType: string
    body?: string
}

export async function saveMailTemplateFor(mailTemplate: MailTemplateRequest): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/email-template`,
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
    await ensureOk(response)
}

export async function getMailTemplateFor(mailType: string): Promise<MailTemplate> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/email-template/${mailType}`,
        {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        })
    return jsonOf<MailTemplate>(response)
}

import { getIdpBaseUrl } from "../../config/ConfigLoader"
import { ensureOk, errorMessageFor, jsonOf } from "../../utils/ApiError"

export const smsTemplatesMessageFor = (error: unknown) => errorMessageFor(error, "SMS templates", "SMS template")

interface SmsTemplate {
    body: string
}

interface SmsTemplateRequest {
    smsType: string
    body?: string
}

export async function saveSmsTemplateFor(smsTemplate: SmsTemplateRequest): Promise<void> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/sms-template`,
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include',
            body: JSON.stringify(smsTemplate),
        })
    await ensureOk(response)
}

export async function getSmsTemplateFor(smsType: string): Promise<SmsTemplate> {
    const baseUrl = await getIdpBaseUrl()

    const response = await fetch(`${baseUrl}/api/sms-template/${smsType}`,
        {
            method: "GET",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.sessionStorage.getItem("ACCESS_TOKEN")}`,
            },
            mode: "cors",
            credentials: 'include'
        })
    return jsonOf<SmsTemplate>(response)
}

import { getIdpBaseUrl } from "../../config/ConfigLoader"

interface SmsTemplateRequest {
    smsType: string
    body?: string
}

export async function saveSmsTemplateFor(smsTemplate: SmsTemplateRequest) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/sms-template`,
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
}

export async function getSmsTemplateFor(smsType: string) {
    const baseUrl = await getIdpBaseUrl()

    return fetch(`${baseUrl}/api/sms-template/${smsType}`,
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

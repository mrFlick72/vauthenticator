interface MailTemplateRequest {
    emailType: string
    body?: string
}

export function saveMailTemplateFor(mailTemplate: MailTemplateRequest) {
    return fetch("/secure/api/email-template",
        {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(mailTemplate),
            credentials: 'same-origin'
        })
}

export function getMailTemplateFor(mailType: string) {
    return fetch(`/secure/api/email-template/${mailType}`,
        {
            method: "GET",
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        }).then(r => r.json())
}

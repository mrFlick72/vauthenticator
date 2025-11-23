type Account = {
    email: string
    password: string
    firstName: string
    lastName: string
    phone: string
    birthDate: string
}

export default function signUp(account: Account) {
    return fetch("/api/accounts",
        {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(account),
            credentials: 'same-origin'
        })
}
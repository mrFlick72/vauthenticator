export async function findAllScopes(): Promise<string[]> {
    let response = await fetch("/scopes",
        {
            method: "GET",
            headers: {
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });
    return await response.json() as string[]
}

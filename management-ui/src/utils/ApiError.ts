/**
 * A management api call that answered with a non-2xx status.
 * It keeps the response body as text, since an error body is not guaranteed to be json.
 */
export class ApiError extends Error {
    constructor(readonly status: number, readonly body: string) {
        super(`management api call failed with HTTP ${status}`)
        this.name = "ApiError"
        // babel compiles to es5, where a subclass of Error loses its prototype and instanceof fails
        Object.setPrototypeOf(this, ApiError.prototype)
    }
}

/**
 * Throws an ApiError unless the response status is 2xx (200, 201, 204...). It never reads a successful body.
 */
export async function ensureOk(response: Response): Promise<Response> {
    if (!response.ok) {
        throw new ApiError(response.status, await response.text())
    }
    return response
}

/**
 * Parses the body of a successful response; use it only for calls whose success carries a body.
 */
export async function jsonOf<T>(response: Response): Promise<T> {
    return (await ensureOk(response)).json() as Promise<T>
}

/**
 * Turns a failed call into a message for the admin.
 *
 * @param managed what the page manages, used when access is denied, e.g. "client applications"
 * @param missing what was looked up, used when it does not exist, e.g. "client application"
 */
export function errorMessageFor(error: unknown, managed: string, missing: string): string {
    if (!(error instanceof ApiError)) {
        return "Unexpected error, the server could not be reached"
    }
    switch (error.status) {
        case 400:
            return error.body ? `The request is not valid: ${error.body}` : "The request is not valid"
        case 401:
            return "Your session has expired, please log in again"
        case 403:
            return `You are not allowed to manage ${managed}`
        case 404:
            return `The requested ${missing} was not found`
        default:
            return `Unexpected error (HTTP ${error.status})`
    }
}

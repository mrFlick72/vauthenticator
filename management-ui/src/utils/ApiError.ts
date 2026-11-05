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
 * What an api resource is called in a failure reason.
 *
 * @property managed what the admin manages through it, used when access is denied, e.g. "client applications"
 * @property missing what a single lookup returns, used when it does not exist, e.g. "client application"
 */
export type ApiSubject = { managed: string, missing: string }

/**
 * Why a call failed, worded for the admin. A status listed in `reasons` overrides the default wording, for a
 * status the api gives a meaning of its own, such as 412 for a protected role.
 */
export function failureReasonFor(error: unknown, subject: ApiSubject, reasons: Record<number, string> = {}): string {
    if (!(error instanceof ApiError)) {
        return "the server could not be reached"
    }
    if (reasons[error.status]) {
        return reasons[error.status]
    }
    switch (error.status) {
        case 400:
            return error.body || "the request is not valid"
        case 401:
            return "your session has expired, please log in again"
        case 403:
            return `you are not allowed to manage ${subject.managed}`
        case 404:
            return `the requested ${subject.missing} was not found`
        default:
            return `unexpected error (HTTP ${error.status})`
    }
}

/**
 * The message for a failed call: what the admin tried to do and why it failed,
 * e.g. "Could not delete the role: it is a protected role".
 *
 * @param action what the call was doing, completing "Could not ...", e.g. "load the keys"
 */
export function failureMessage(action: string,
                               error: unknown,
                               subject: ApiSubject,
                               reasons: Record<number, string> = {}): string {
    return `Could not ${action}: ${failureReasonFor(error, subject, reasons)}`
}

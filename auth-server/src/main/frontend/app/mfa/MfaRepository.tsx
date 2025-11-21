export type MfaAccountEnrolledMethod = {
    userName: string
    mfaMethod: string
    mfaChannel: string
    mfaDeviceId: string,
    default: boolean
}

export async function sendMfaCode(mfaDeviceId : string): Promise<void> {
    await fetch(`/api/mfa/challenge?mfa-device-id=${mfaDeviceId}`, {
        method: 'PUT', // *GET, POST, PUT, DELETE, etc.
        credentials: 'same-origin', // include, *same-origin, omit
    });
    return new Promise(resolve => resolve());
}

export async function getMfaMethods(): Promise<MfaAccountEnrolledMethod[]> {
    let response = await fetch("/api/mfa/enrollment", {
        method: 'GET', // *GET, POST, PUT, DELETE, etc.
        credentials: 'same-origin', // include, *same-origin, omit
    });
    return await response.json() as MfaAccountEnrolledMethod[];
}
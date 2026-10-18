package com.vauthenticator.server.mfa.domain

interface MfaAccountMethodsRepository {

    fun findBy(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String): MfaAccountMethod?
    fun findBy(deviceId: MfaDeviceId): MfaAccountMethod?
    fun findAll(userName: String): List<MfaAccountMethod>
    fun save(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String, associated: Boolean): MfaAccountMethod

    fun setAsDefault(userName: String, deviceId: MfaDeviceId)
    fun getDefaultDevice(userName: String): MfaDeviceId?

    /**
     * Deletes the account's device after [check] accepts it, as one step: an adapter that can, holds the
     * account's devices locked from the check to the delete. Throws [MfaDeviceNotFoundException] when the
     * account has no device with [mfaDeviceId].
     */
    fun revoke(userName: String, mfaDeviceId: MfaDeviceId, check: MfaDeviceRevocationCheck): MfaAccountMethod
}

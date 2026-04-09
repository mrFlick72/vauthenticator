package com.vauthenticator.server.mfa.domain

interface MfaAccountMethodsRepository {

    fun findBy(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String): MfaAccountMethod?
    fun findBy(deviceId: MfaDeviceId): MfaAccountMethod?
    fun findAll(userName: String): List<MfaAccountMethod>
    fun save(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String, associated: Boolean): MfaAccountMethod

    fun setAsDefault(userName: String, deviceId: MfaDeviceId)
    fun getDefaultDevice(userName: String): MfaDeviceId?
}

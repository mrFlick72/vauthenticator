package com.vauthenticator.server.mfa.domain

import java.util.*

interface MfaAccountMethodsRepository {

    fun findBy(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String): Optional<MfaAccountMethod>
    fun findBy(deviceId: MfaDeviceId): Optional<MfaAccountMethod>
    fun findAll(userName: String): List<MfaAccountMethod>
    fun save(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String, associated: Boolean): MfaAccountMethod

    fun setAsDefault(userName: String, deviceId: MfaDeviceId)
    fun getDefaultDevice(userName: String): Optional<MfaDeviceId>
}


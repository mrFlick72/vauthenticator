package com.vauthenticator.server.support

import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.AccountTestFixture.anAccountWithPhoneNumber
import java.util.*

object MfaFixture {

    fun associatedMfaAccountMethod(userName: String, mfaChannel: String, mfaMethod: MfaMethod) =
        Optional.of(MfaAccountMethod(userName, mfaDeviceId, keyId, mfaMethod, mfaChannel, true))

    fun notAssociatedMfaAccountMethod(userName: String, mfaChannel: String, mfaMethod: MfaMethod) =
        Optional.of(MfaAccountMethod(userName, mfaDeviceId, keyId, mfaMethod, mfaChannel, false))

    val account = anAccount()
    val accountWithPhone = anAccountWithPhoneNumber()
    val userName = account.email
    val formattedPhone = accountWithPhone.phone.get().formattedPhone()
    const val email = "a_new_email@email.com"
    val challenge = MfaChallenge("AN_MFA_CHALLENGE")
    val mfaDeviceId = MfaDeviceId("AN_MFA_DEVICE_ID")
    val mfaSecret = MfaSecret("AN_MFA_SECRET")
    val keyId = Kid("A_KID")

}
package com.vauthenticator.server.mask

import com.vauthenticator.server.mfa.domain.MfaMethod

interface SensitiveDataMasker {

    fun mask(sensitiveData: String): String
}

class SensitiveDataMaskerResolver(private val registry: Map<MfaMethod, SensitiveDataMasker>) {

    fun getSensitiveDataMasker(mfaMethod: MfaMethod): SensitiveDataMasker = registry[mfaMethod]!!
}

class SensitiveEmailMasker : SensitiveDataMasker {
    override fun mask(sensitiveData: String): String {
        val dividedMail = sensitiveData.split("@")
        val email = dividedMail[0]
        val mailDomain = dividedMail[1]

        val maskedMail = email.replaceRange(1, email.length, "x".repeat(email.length - 1))

        return "%s@%s".format(maskedMail, mailDomain)
    }

}

class SensitivePhoneMasker : SensitiveDataMasker {
    override fun mask(sensitiveData: String): String {
        return sensitiveData.replaceRange(7, sensitiveData.length - 3, "x".repeat(sensitiveData.length - 10))
    }

}
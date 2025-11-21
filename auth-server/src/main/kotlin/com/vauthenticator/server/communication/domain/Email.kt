package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.account.domain.Account

fun interface EMailSenderService {
    fun sendFor(account: Account, emailContext: MessageContext)
}

fun interface EMailMessageFactory {
    fun makeMailMessageFor(account: Account, requestContext: MessageContext): EMailMessage
}

class SimpleEMailMessageFactory(val from: String, val subject: String, private val emailType: EMailType) :
    EMailMessageFactory {

    override fun makeMailMessageFor(account: Account, requestContext: MessageContext): EMailMessage {
        val context = messageContextFrom(account) + requestContext
        return EMailMessage(context["email"] as String, from, subject, emailType, context)
    }

}

enum class EMailType(val path: String) {
    WELCOME("templates/welcome.html"),
    EMAIL_VERIFICATION("templates/email-verify-challenge.html"),
    RESET_PASSWORD("templates/reset-password.html"),
    MFA("templates/mfa-challenge.html");
}

data class EMailMessage(
    val to: String,
    val from: String,
    val subject: String,
    val type: EMailType = EMailType.WELCOME,
    val context: MessageContext
)

data class EMailTemplate(val emailType: EMailType, val body: String)

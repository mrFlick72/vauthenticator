package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.account.domain.Account

data class SmsMessage(val phoneNumber: String, val message: String)

class InvalidSmsMessageContextException(message: String) : RuntimeException(message)

fun interface SmsSenderService {
    fun sendFor(account: Account, smsContext: MessageContext)
}

fun interface SmsMessageFactory {
    fun makeSmsMessageFor(account: Account, requestContext: MessageContext): SmsMessage
}

class SimpleSmsMessageFactory :
    SmsMessageFactory {

    override fun makeSmsMessageFor(account: Account, requestContext: MessageContext): SmsMessage {
        val context = messageContextFrom(account) + requestContext

        return SmsMessage(
            phoneNumber = context.requiredString("phone"),
            message = requestContext.requiredString("mfaCode")
        )
    }

    private fun MessageContext.requiredString(key: String): String {
        val value = this[key] ?: throw InvalidSmsMessageContextException("The sms context is not valid, missing $key")
        val stringValue = value as? String
            ?: throw InvalidSmsMessageContextException("The sms context is not valid, $key must be a String")

        return stringValue.takeIf { it.isNotBlank() }
            ?: throw InvalidSmsMessageContextException("The sms context is not valid, blank $key")
    }

}

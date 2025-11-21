package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.account.domain.Account

data class SmsMessage(val phoneNumber: String, val message: String)


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
        return SmsMessage(context["phone"]!! as String, requestContext["mfaCode"]!! as String)
    }


}

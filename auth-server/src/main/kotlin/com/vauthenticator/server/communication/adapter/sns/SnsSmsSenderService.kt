package com.vauthenticator.server.communication.adapter.sns

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.communication.domain.MessageContext
import com.vauthenticator.server.communication.domain.SmsMessageFactory
import com.vauthenticator.server.communication.domain.SmsSenderService
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest


class SnsSmsSenderService(
    private val snsClient: SnsClient,
    private val smsMessageFactory: SmsMessageFactory
) : SmsSenderService {
    override fun sendFor(account: Account, smsContext: MessageContext) {
        val smsMessage = smsMessageFactory.makeSmsMessageFor(account, smsContext)
        snsClient.publish(
            PublishRequest.builder()
                .message(smsMessage.message)
                .phoneNumber(smsMessage.phoneNumber.replace(" ", ""))
                .build()
        )
    }

}
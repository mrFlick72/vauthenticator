package com.vauthenticator.server.communication.adapter.sns

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.communication.domain.MessageContext
import com.vauthenticator.server.communication.domain.MessageTemplateResolver
import com.vauthenticator.server.communication.domain.SmsMessage
import com.vauthenticator.server.communication.domain.SmsMessageFactory
import com.vauthenticator.server.communication.domain.SmsSenderService
import com.vauthenticator.platform.document.domain.DocumentRepository
import com.vauthenticator.server.communication.domain.CommunicationDocumentType
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest


class SnsSmsSenderService(
    private val snsClient: SnsClient,
    private val documentRepository: DocumentRepository,
    private val templateResolver: MessageTemplateResolver,
    private val smsMessageFactory: SmsMessageFactory
) : SmsSenderService {
    override fun sendFor(account: Account, smsContext: MessageContext) {
        val smsMessage = smsMessageFactory.makeSmsMessageFor(account, smsContext)
        val smsContent = templateResolver.compile(smsTemplateFor(smsMessage), smsMessage.context)
        snsClient.publish(
            PublishRequest.builder()
                .message(smsContent)
                .phoneNumber(smsMessage.phoneNumber.replace(" ", ""))
                .build()
        )
    }

    private fun smsTemplateFor(sms: SmsMessage): String {
        val documentContent = documentRepository.loadDocument(CommunicationDocumentType.SMS.content, sms.type.path)
        return String(documentContent.content)
    }

}

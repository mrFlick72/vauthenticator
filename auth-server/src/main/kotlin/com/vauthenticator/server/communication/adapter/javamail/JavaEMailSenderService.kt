package com.vauthenticator.server.communication.adapter.javamail

import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.communication.domain.*
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper


class JavaEMailSenderService(
    private val documentRepository: DocumentRepository,
    private val mailSender: JavaMailSender,
    private val templateResolver: MessageTemplateResolver,
    private val emailMessageFactory: EMailMessageFactory
) : EMailSenderService {

    override fun sendFor(account: Account, emailContext: MessageContext) {
        val mailMessage = emailMessageFactory.makeMailMessageFor(account, emailContext)
        val mailContent = mailTemplateFor(mailMessage)
        val email = composeMailFor(mailContent, mailMessage)
        mailSender.send(email)
    }

    private fun mailTemplateFor(email: EMailMessage): String {
        val documentContent =
            documentRepository.loadDocument(DocumentType.EMAIL.content, mailTemplatePathFor(email.type))
        return String(documentContent.content)
    }

    private fun mailTemplatePathFor(emailType: EMailType): String = emailType.path

    private fun composeMailFor(mailContent: String, emailMessage: EMailMessage): MimeMessage {
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()

        val helper = MimeMessageHelper(mimeMessage, "utf-8")
        helper.setText(templateResolver.compile(mailContent, emailMessage.context), true) // Use this or above line.
        helper.setTo(emailMessage.to)
        helper.setSubject(emailMessage.subject)
        helper.setFrom(emailMessage.from)

        return mimeMessage
    }

}
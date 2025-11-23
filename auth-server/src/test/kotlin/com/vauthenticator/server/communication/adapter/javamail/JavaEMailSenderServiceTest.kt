package com.vauthenticator.server.communication.adapter.javamail

import com.hubspot.jinjava.Jinjava
import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.junit5.GreenMailExtension
import com.icegreen.greenmail.util.ServerSetupTest
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.communication.adapter.JinJavaTemplateResolver
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.communication.domain.MessageTemplateResolver
import com.vauthenticator.server.communication.domain.SimpleEMailMessageFactory
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.document.domain.DocumentType.*
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.mail.javamail.JavaMailSenderImpl

class JavaEMailSenderServiceTest {

    @RegisterExtension
    val greenMail: GreenMailExtension = GreenMailExtension(ServerSetupTest.SMTP)
        .withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "pwd"))
        .withPerMethodLifecycle(true)


    private val email = "email@mail.com"
    private val subject = "test"

    private lateinit var emailSenderService: EMailSenderService
    private val documentRepository: DocumentRepository = mockk()

    private val templateResolver: MessageTemplateResolver = JinJavaTemplateResolver(Jinjava())

    private fun newJavaMail(): JavaMailSenderImpl {
        val javaMailSender = JavaMailSenderImpl()
        javaMailSender.port = greenMail.smtp.port
        javaMailSender.host = "127.0.0.1"
        javaMailSender.username = "user"
        return javaMailSender
    }

    private fun simpleMailMessageFactoryFor(emailType: EMailType) = SimpleEMailMessageFactory(email, subject, emailType)

    @Test
    internal fun `when a welcome mail is sent`() {
        val account = anAccount().copy(firstName = "John", lastName = "Miller")
        val mailTemplateContent =
            "hi {{ firstName }} {{ lastName }} your signup to vauthenticator succeeded".toByteArray()

        emailSenderService = JavaEMailSenderService(
            documentRepository, newJavaMail(), templateResolver, simpleMailMessageFactoryFor(
                EMailType.WELCOME
            )
        )

        every {
            documentRepository.loadDocument(
                EMAIL.content,
                EMailType.WELCOME.path
            ).content
        } returns mailTemplateContent


        emailSenderService.sendFor(account, emptyMap())
        val mail = greenMail.receivedMessages[0]
        assertEquals(account.email, mail.allRecipients[0].toString())
        assertEquals(this.email, mail.from[0].toString())
        assertEquals(subject, mail.subject)
        assertEquals("hi John Miller your signup to vauthenticator succeeded", mail.content.toString().trim())
    }


    @Test
    internal fun `when a email verification mail is sent`() {
        val account = anAccount().copy(firstName = "John", lastName = "Miller")
        val mailTemplateContent =
            "hi {{ firstName }} {{ lastName }} in order to verify your mail click <a href='{{ emailVerificationTicket }}'>here</a>".toByteArray()

        emailSenderService = JavaEMailSenderService(
            documentRepository, newJavaMail(), templateResolver, simpleMailMessageFactoryFor(
                EMailType.EMAIL_VERIFICATION
            )
        )

        every {
            documentRepository.loadDocument(
                EMAIL.content,
                EMailType.EMAIL_VERIFICATION.path
            ).content
        } returns mailTemplateContent

        emailSenderService.sendFor(
            account,
            mapOf("emailVerificationTicket" to "https://vauthenticator.com/email-verify/uuid-1234-uuid")
        )
        val mail = greenMail.receivedMessages[0]
        assertEquals(account.email, mail.allRecipients[0].toString())
        assertEquals(this.email, mail.from[0].toString())
        assertEquals(subject, mail.subject)
        assertEquals(
            "hi John Miller in order to verify your mail click <a href='https://vauthenticator.com/email-verify/uuid-1234-uuid'>here</a>",
            mail.content.toString().trim()
        )
    }

    @Test
    internal fun `when a reset password mail is sent`() {
        val account = anAccount().copy(firstName = "John", lastName = "Miller")
        val mailTemplateContent =
            "hi {{ firstName }} {{ lastName }} in order to reset your password click <a href='{{ resetPasswordLink }}'>here</a>".toByteArray()

        emailSenderService = JavaEMailSenderService(
            documentRepository, newJavaMail(), templateResolver, simpleMailMessageFactoryFor(
                EMailType.RESET_PASSWORD
            )
        )

        every {
            documentRepository.loadDocument(
                EMAIL.content,
                EMailType.RESET_PASSWORD.path
            ).content
        } returns mailTemplateContent

        emailSenderService.sendFor(
            account,
            mapOf("resetPasswordLink" to "https://vauthenticator.com/reset-password/uuid-1234-uuid")
        )
        val mail = greenMail.receivedMessages[0]
        assertEquals(account.email, mail.allRecipients[0].toString())
        assertEquals(this.email, mail.from[0].toString())
        assertEquals(subject, mail.subject)
        assertEquals(
            "hi John Miller in order to reset your password click <a href='https://vauthenticator.com/reset-password/uuid-1234-uuid'>here</a>",
            mail.content.toString().trim()
        )
    }
}
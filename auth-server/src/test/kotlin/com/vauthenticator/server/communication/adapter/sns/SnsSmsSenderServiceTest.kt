package com.vauthenticator.server.communication.adapter.sns

import com.hubspot.jinjava.Jinjava
import com.vauthenticator.server.account.domain.Phone.Companion.phoneFor
import com.vauthenticator.server.communication.adapter.JinJavaTemplateResolver
import com.vauthenticator.server.communication.domain.SimpleSmsMessageFactory
import com.vauthenticator.server.communication.domain.SmsType
import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.SmsUtils
import com.vauthenticator.server.support.SmsUtils.resetSmsProvider
import com.vauthenticator.server.support.SmsUtils.snsClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.NoSuchFileException

@ExtendWith(MockKExtension::class)
class SnsSmsSenderServiceTest {

    @MockK
    lateinit var documentRepository: DocumentRepository

    @AfterEach
    fun tearDown() {
        resetSmsProvider()
    }

    @Test
    fun `when an mfa sms is sent its body is rendered from the mfa sms template`() {
        val uut = SnsSmsSenderService(
            snsClient,
            documentRepository,
            JinJavaTemplateResolver(Jinjava()),
            SimpleSmsMessageFactory(SmsType.MFA)
        )
        val account = anAccount().copy(firstName = "John", phone = phoneFor("+39 339 2323233"))

        every {
            documentRepository.loadDocument(DocumentType.SMS.content, SmsType.MFA.path)
        } returns Document("text/plain", SmsType.MFA.path, "hi {{ firstName }} your code is {{ mfaCode }}".toByteArray())

        uut.sendFor(account, mapOf("mfaCode" to "123456"))

        val actual = SmsUtils.getMessageFor("+393392323233")
        assertEquals("hi John your code is 123456", actual)
    }

    @Test
    fun `an mfa sms is not sent when the mfa sms template is missing`() {
        val uut = SnsSmsSenderService(
            snsClient,
            documentRepository,
            JinJavaTemplateResolver(Jinjava()),
            SimpleSmsMessageFactory(SmsType.MFA)
        )
        val account = anAccount().copy(phone = phoneFor("+39 339 2323233"))

        every {
            documentRepository.loadDocument(DocumentType.SMS.content, SmsType.MFA.path)
        } throws NoSuchFileException(SmsType.MFA.path)

        assertThrows(NoSuchFileException::class.java) {
            uut.sendFor(account, mapOf("mfaCode" to "123456"))
        }

        assertEquals("", SmsUtils.getMessageFor("+393392323233"))
    }
}

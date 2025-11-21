package com.vauthenticator.server.communication.adapter.sns

import com.vauthenticator.server.account.domain.Phone.Companion.phoneFor
import com.vauthenticator.server.communication.domain.SmsMessage
import com.vauthenticator.server.communication.domain.SmsMessageFactory
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.SmsUtils
import com.vauthenticator.server.support.SmsUtils.resetSmsProvider
import com.vauthenticator.server.support.SmsUtils.snsClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SnsSmsSenderServiceTest {

    @MockK
    lateinit var smsMessageFactory: SmsMessageFactory

    @AfterEach
    fun tearDown() {
        resetSmsProvider()
    }

    @Test
    fun `happy path`() {
        val uut = SnsSmsSenderService(snsClient, smsMessageFactory)

        val account = anAccount().copy(phone = phoneFor("+39 339 2323233"))
        every { smsMessageFactory.makeSmsMessageFor(account, emptyMap()) } returns SmsMessage(account.phone.get().formattedPhone(), "it is an sms message")
        uut.sendFor(account, emptyMap())

        val actual = SmsUtils.getMessageFor("+393392323233")
        assertEquals("it is an sms message", actual)
    }
}

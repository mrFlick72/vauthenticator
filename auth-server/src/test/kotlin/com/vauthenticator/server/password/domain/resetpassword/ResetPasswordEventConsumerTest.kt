package com.vauthenticator.server.password.domain.resetpassword

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.ResetPasswordEvent
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class ResetPasswordEventConsumerTest {

    @MockK
    lateinit var passwordHistoryRepository: PasswordHistoryRepository

    @Test
    fun `when upon a sign up event the password history is updated`() {
        val password = Password("A_PASSWORD")
        val userName = Email("AN_EMAIL")
        val event = ResetPasswordEvent(userName, ClientAppFixture.aClientAppId(), Instant.now(), password)

        val uut = ResetPasswordEventConsumer(passwordHistoryRepository)

        every { passwordHistoryRepository.store(userName.content, password) } just runs

        uut.accept(event)

        verify { passwordHistoryRepository.store(userName.content, password) }
    }
}
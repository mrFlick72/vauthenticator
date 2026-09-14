package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.support.passwordLifeCycleRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleExecutorTest {

    @MockK
    lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository
    @MockK
    lateinit var passwordResetStrategy:  PasswordLifeCycleStrategy

    lateinit var uut: PasswordLifeCycleExecutor


    @BeforeEach
    fun setUp() {
        uut = PasswordLifeCycleExecutor(passwordLifeCycleRepository, listOf(passwordResetStrategy))
    }

    @Test
    fun `when a new password lifecycle rule is registered`() {
        every { passwordLifeCycleRepository.store(passwordLifeCycleRule) } just runs
        uut.register(passwordLifeCycleRule)
        verify { passwordLifeCycleRepository.store(passwordLifeCycleRule) }
    }

    @Test
    fun `when a password reset is the action to be executed`() {
        every { passwordResetStrategy.canHandle(passwordLifeCycleRule) } returns true
        every { passwordResetStrategy.execute(passwordLifeCycleRule) } just runs
        uut.execute(passwordLifeCycleRule)
        verify { passwordResetStrategy.execute(passwordLifeCycleRule) }
    }
    @Test
    fun `when a strategy requested can not be handled`() {
        every { passwordResetStrategy.canHandle(passwordLifeCycleRule) } returns false
        assertThrows<NoopPasswordLifeCycleStrategyException> {
            uut.execute(passwordLifeCycleRule)
        }
    }
}
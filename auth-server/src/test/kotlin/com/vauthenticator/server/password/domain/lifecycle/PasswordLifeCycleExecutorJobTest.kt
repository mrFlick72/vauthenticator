package com.vauthenticator.server.password.domain.lifecycle

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleExecutorJobTest {

    @MockK
    private lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository

    @MockK
    private lateinit var passwordLifeCycleExecutor: PasswordLifeCycleExecutor

    lateinit var uut: PasswordLifeCycleExecutorJob

    @BeforeEach
    fun setUp() {
        uut = PasswordLifeCycleExecutorJob(passwordLifeCycleRepository, passwordLifeCycleExecutor)
    }

    @Test
    fun `when a lot of rules are processed`() {
        every { passwordLifeCycleRepository.findAllRules(page = 0, size = 100) } returns listOf(
            PasswordLifeCycleRule(
                userName = "user_1",
                ttl =                 Duration.ZERO,
                creationDate = LocalDateTime.now(),
                lastEvaluationDate = null,
                action = PasswordLifeCycleAction.ACCOUNT_LOCK
            )
        )
        every { passwordLifeCycleRepository.findAllRules(page = 1, size = 100) } returns listOf(
            PasswordLifeCycleRule(
                userName = "user_1",
                ttl =                 Duration.ZERO,
                creationDate = LocalDateTime.now(),
                lastEvaluationDate = null,
                action = PasswordLifeCycleAction.ACCOUNT_LOCK
            )
        )
        every { passwordLifeCycleRepository.findAllRules(page = 2, size = 100) } returns listOf(
            PasswordLifeCycleRule(
                userName = "user_2",
                ttl =                 Duration.ZERO,
                creationDate = LocalDateTime.now(),
                lastEvaluationDate = null,
                action = PasswordLifeCycleAction.ACCOUNT_LOCK
            )
        )

        every { passwordLifeCycleRepository.findAllRules(page = 3, size = 100) } returns emptyList()

        every {
            passwordLifeCycleExecutor.execute(
                PasswordLifeCycleRule(
                    userName = "user_1",
                    ttl =                 Duration.ZERO,
                    creationDate = LocalDateTime.now(),
                    lastEvaluationDate = null,
                    action = PasswordLifeCycleAction.ACCOUNT_LOCK
                )
            )
        } just runs

        every { passwordLifeCycleRepository.findAllRules(page = 3, size = 100) } returns emptyList()

        every {
            passwordLifeCycleExecutor.execute(
                PasswordLifeCycleRule(
                    userName = "user_1",
                    ttl =                 Duration.ZERO,
                    creationDate = LocalDateTime.now(),
                    lastEvaluationDate = null,
                    action = PasswordLifeCycleAction.ACCOUNT_LOCK
                )
            )
        } just runs
        every {
            passwordLifeCycleExecutor.execute(
                PasswordLifeCycleRule(
                    userName = "user_2",
                    ttl =                 Duration.ZERO,
                    creationDate = LocalDateTime.now(),
                    lastEvaluationDate = null,
                    action = PasswordLifeCycleAction.ACCOUNT_LOCK
                )
            )
        } just runs

        uut.execute()

        verify { passwordLifeCycleRepository.findAllRules(page = 0, size = 100) }
        verify { passwordLifeCycleRepository.findAllRules(page = 1, size = 100) }
        verify { passwordLifeCycleRepository.findAllRules(page = 2, size = 100) }
        verify { passwordLifeCycleRepository.findAllRules(page = 3, size = 100) }
    }

    @Test
    fun `when no rules are processed`() {
        every { passwordLifeCycleRepository.findAllRules(page = 0, size = 100) }  returns emptyList()

        uut.execute()

        verify { passwordLifeCycleRepository.findAllRules(page = 0, size = 100) }
    }
}
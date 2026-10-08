package com.vauthenticator.server.password.api

import java.time.Duration
import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.domain.lifecycle.InvalidPasswordLifeCycleIntervalException
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction.ACCOUNT_LOCK
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRules
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.support.passwordLifeCycleRule
import com.vauthenticator.server.web.ExceptionAdviceController
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

private const val RULES_PATH = "/api/admin/accounts/$EMAIL/password/lifecycle"
private const val LOCK_RULE_PATH = "$RULES_PATH/ACCOUNT_LOCK"

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleEndPointTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var passwordLifeCycleRules: PasswordLifeCycleRules

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val editor = m2mPrincipalFor("m2m", listOf("admin:password-lifecycle-editor"))
    private val notAnEditor = m2mPrincipalFor("m2m", listOf("admin:whatever"))

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            PasswordLifeCycleEndPoint(
                PermissionValidator(clientApplicationRepository),
                passwordLifeCycleRules
            )
        ).setControllerAdvice(ExceptionAdviceController())
            .build()
    }

    @Test
    fun `when a rule is registered`() {
        every { passwordLifeCycleRules.register(EMAIL, ACCOUNT_LOCK, Duration.ofDays(90)) } just runs

        mokMvc.perform(
            put(LOCK_RULE_PATH)
                .principal(editor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"intervalSeconds": 7776000}""")
        ).andExpect(status().isNoContent)

        verify { passwordLifeCycleRules.register(EMAIL, ACCOUNT_LOCK, Duration.ofDays(90)) }
    }

    @Test
    fun `when a rule is registered for an unknown account`() {
        every { passwordLifeCycleRules.register(EMAIL, ACCOUNT_LOCK, Duration.ofDays(90)) } throws
                AccountNotFoundException("missing")

        mokMvc.perform(
            put(LOCK_RULE_PATH)
                .principal(editor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"intervalSeconds": 7776000}""")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `when a rule is registered with an interval beyond the supported date range`() {
        every { passwordLifeCycleRules.register(EMAIL, ACCOUNT_LOCK, Duration.ofSeconds(Long.MAX_VALUE)) } throws
                InvalidPasswordLifeCycleIntervalException("out of range")

        mokMvc.perform(
            put(LOCK_RULE_PATH)
                .principal(editor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"intervalSeconds": 9223372036854775807}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `when a rule is registered with a non positive interval`() {
        every { passwordLifeCycleRules.register(EMAIL, ACCOUNT_LOCK, Duration.ZERO) } throws
                InvalidPasswordLifeCycleIntervalException("not positive")

        mokMvc.perform(
            put(LOCK_RULE_PATH)
                .principal(editor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"intervalSeconds": 0}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `when a rule is registered with an interval that is not a number of seconds`() {
        listOf("""{"intervalSeconds": "P90D"}""", """{"intervalSeconds": "90 days"}""", """{"intervalSeconds": null}""", """{}""").forEach { body ->
            mokMvc.perform(
                put(LOCK_RULE_PATH)
                    .principal(editor)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            ).andExpect(status().isBadRequest)
        }

        verify(exactly = 0) { passwordLifeCycleRules.register(any(), any(), any()) }
    }

    @Test
    fun `when a rule is registered for an unknown action`() {
        mokMvc.perform(
            put("$RULES_PATH/WHATEVER")
                .principal(editor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"intervalSeconds": 7776000}""")
        ).andExpect(status().isBadRequest)

        verify(exactly = 0) { passwordLifeCycleRules.register(any(), any(), any()) }
    }

    @Test
    fun `when a rule is registered without the editor scope`() {
        mokMvc.perform(
            put(LOCK_RULE_PATH)
                .principal(notAnEditor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"intervalSeconds": 7776000}""")
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { passwordLifeCycleRules.register(any(), any(), any()) }
    }

    @Test
    fun `when the rules of an account are retrieved`() {
        every { passwordLifeCycleRules.rulesFor(EMAIL) } returns listOf(passwordLifeCycleRule)

        mokMvc.perform(get(RULES_PATH).principal(editor))
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    [{
                      "action": "PASSWORD_RESET",
                      "intervalSeconds": 3600,
                      "creationDate": "2026-10-01T10:00",
                      "lastEvaluationDate": null,
                      "nextEvaluationDate": "2026-10-01T11:00"
                    }]
                    """
                )
            )
    }

    @Test
    fun `when the rules of an account are retrieved without the editor scope`() {
        mokMvc.perform(get(RULES_PATH).principal(notAnEditor))
            .andExpect(status().isForbidden)

        verify(exactly = 0) { passwordLifeCycleRules.rulesFor(any()) }
    }

    @Test
    fun `when a rule is removed`() {
        every { passwordLifeCycleRules.remove(EMAIL, ACCOUNT_LOCK) } just runs

        mokMvc.perform(delete(LOCK_RULE_PATH).principal(editor))
            .andExpect(status().isNoContent)

        verify { passwordLifeCycleRules.remove(EMAIL, ACCOUNT_LOCK) }
    }

    @Test
    fun `when a rule is removed without the editor scope`() {
        mokMvc.perform(delete(LOCK_RULE_PATH).principal(notAnEditor))
            .andExpect(status().isForbidden)

        verify(exactly = 0) { passwordLifeCycleRules.remove(any(), any()) }
    }
}

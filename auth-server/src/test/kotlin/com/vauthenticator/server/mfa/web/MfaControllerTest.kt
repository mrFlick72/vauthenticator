package com.vauthenticator.server.mfa.web

import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.SecurityFixture.principalFor
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class MfaControllerTest {
    lateinit var mokMvc: MockMvc

    @MockK
    private lateinit var successHandler: AuthenticationSuccessHandler

    @MockK
    private lateinit var failureHandler: AuthenticationFailureHandler

    @MockK
    private lateinit var publisher: ApplicationEventPublisher

    @MockK
    private lateinit var i18nMessageInjector: I18nMessageInjector

    @MockK
    private lateinit var mfaChallengeSender: MfaChallengeSender

    @MockK
    private lateinit var mfaVerifier: MfaVerifier

    private val account = anAccount()

    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(
            MfaController(
                i18nMessageInjector,
                publisher,
                successHandler,
                failureHandler,
                mfaChallengeSender,
                mfaVerifier
            )
        ).build()
    }

    @Test
    internal fun `when an mfa challenge is sent`() {
        every { mfaChallengeSender.sendMfaChallengeFor(account.email) } just runs

        mokMvc.perform(
            get("/mfa-challenge/send")
                .principal(principalFor(account.email))
        ).andExpect(redirectedUrl("/mfa-challenge"))
        verify { mfaChallengeSender.sendMfaChallengeFor(account.email) }
    }

    @Test
    internal fun `when an mfa challenge is rendered`() {
        every { i18nMessageInjector.setMessagedFor(I18nScope.MFA_PAGE, any()) } just runs

        mokMvc.perform(
            get("/mfa-challenge")
                .principal(principalFor(account.email))
        ).andExpect(view().name("template"))

        verify { i18nMessageInjector.setMessagedFor(I18nScope.MFA_PAGE, any()) }
    }

    @Test
    internal fun `when an mfa challenge is verified via default mfa device`() {
        val userName = account.email
        val mfaAuthentication = principalFor(userName)

        every {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        } just runs
        every { publisher.publishEvent(MfaSuccessEvent(mfaAuthentication)) } just runs
        every { successHandler.onAuthenticationSuccess(any(), any(), mfaAuthentication) } just runs

        mokMvc.perform(
            post("/mfa-challenge")
                .param("mfa-code", "AN_MFA_CHALLENGE_CODE")
                .principal(mfaAuthentication)
        )

        verify { publisher.publishEvent(MfaSuccessEvent(mfaAuthentication)) }
        verify {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        }
        verify { successHandler.onAuthenticationSuccess(any(), any(), mfaAuthentication) }
    }

    @Test
    internal fun `when an mfa challenge is verified with a specific mfa device`() {
        val userName = account.email
        val mfaAuthentication = principalFor(userName)

        every {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaDeviceId("A_MFA_DEVICE_ID"),
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        } just runs
        every { publisher.publishEvent(MfaSuccessEvent(mfaAuthentication)) } just runs
        every { successHandler.onAuthenticationSuccess(any(), any(), mfaAuthentication) } just runs

        mokMvc.perform(
            post("/mfa-challenge")
                .param("mfa-code", "AN_MFA_CHALLENGE_CODE")
                .param("mfa-device-id", "A_MFA_DEVICE_ID")
                .principal(mfaAuthentication)
        )

        verify { publisher.publishEvent(MfaSuccessEvent(mfaAuthentication)) }
        verify {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaDeviceId("A_MFA_DEVICE_ID"),
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        }
        verify { successHandler.onAuthenticationSuccess(any(), any(), mfaAuthentication) }
    }

    @Test
    internal fun `when an mfa challenge is verified via default mfa device fail`() {
        val userName = account.email
        val mfaAuthentication = principalFor(userName)
        val mfaException = MfaException("Invalid mfa code")
        val mfaFailureEvent = MfaFailureEvent(mfaAuthentication, mfaException)

        every {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        } throws mfaException
        every { publisher.publishEvent(mfaFailureEvent) } just runs
        every { failureHandler.onAuthenticationFailure(any(), any(), mfaException) } just runs

        mokMvc.perform(
            post("/mfa-challenge")
                .param("mfa-code", "AN_MFA_CHALLENGE_CODE")
                .principal(mfaAuthentication)
        )

        verify {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        }
        verify { publisher.publishEvent(mfaFailureEvent) }
        verify { failureHandler.onAuthenticationFailure(any(), any(), mfaException) }
    }

    @Test
    internal fun `when an mfa challenge is verified with a specific mfa device fail`() {
        val userName = account.email
        val mfaAuthentication = principalFor(userName)
        val mfaException = MfaException("Invalid mfa code")
        val mfaFailureEvent = MfaFailureEvent(mfaAuthentication, mfaException)

        every {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaDeviceId("A_MFA_DEVICE_ID"),
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        } throws mfaException
        every { publisher.publishEvent(mfaFailureEvent) } just runs
        every { failureHandler.onAuthenticationFailure(any(), any(), mfaException) } just runs

        mokMvc.perform(
            post("/mfa-challenge")
                .param("mfa-code", "AN_MFA_CHALLENGE_CODE")
                .param("mfa-device-id", "A_MFA_DEVICE_ID")
                .principal(mfaAuthentication)
        )

        verify {
            mfaVerifier.verifyAssociatedMfaChallengeFor(
                userName,
                MfaDeviceId("A_MFA_DEVICE_ID"),
                MfaChallenge("AN_MFA_CHALLENGE_CODE")
            )
        }
        verify { publisher.publishEvent(mfaFailureEvent) }
        verify { failureHandler.onAuthenticationFailure(any(), any(), mfaException) }
    }
}
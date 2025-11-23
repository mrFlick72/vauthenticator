package com.vauthenticator.server.mfa.api

import com.vauthenticator.server.mfa.domain.MfaChallengeSender
import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.SecurityFixture
import com.vauthenticator.server.web.ExceptionAdviceController
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup


@ExtendWith(MockKExtension::class)
class MfaChallengeEndPointApiUsageTest {
    lateinit var mokMvc: MockMvc

    private val account = anAccount()

    @MockK
    private lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    private lateinit var mfaChallengeSender: MfaChallengeSender

    @BeforeEach
    fun setUp() {
        val permissionValidator = PermissionValidator(clientApplicationRepository)
        val mfaChallengeEndPoint = MfaChallengeEndPoint(permissionValidator, mfaChallengeSender)
        mokMvc = standaloneSetup(mfaChallengeEndPoint)
            .setControllerAdvice(ExceptionAdviceController())
            .build()
    }

    private val principalWithValidScopes =
        SecurityFixture.principalFor(A_CLIENT_APP_ID, account.email, scopes = listOf(Scope.MFA_ALWAYS.content))
    private val principalWithoutValidScopes =
        SecurityFixture.principalFor(A_CLIENT_APP_ID, account.email, scopes = listOf(Scope.OPEN_ID.content))


    @Test
    fun `when an mfa challenge is sent to the default mfa device`() {
        every { mfaChallengeSender.sendMfaChallengeFor(account.email) } just runs

        mokMvc.perform(
            put("/api/mfa/challenge")
                .principal(principalWithValidScopes)
        ).andExpect(status().isOk)
    }

    @Test
    fun `when an mfa challenge is sent to a specific mfa device`() {
        val mfaDeviceId = MfaDeviceId("A_WELL_DEFINED_MFA_DEVICE_ID")
        every { mfaChallengeSender.sendMfaChallengeFor(account.email, mfaDeviceId) } just runs

        mokMvc.perform(
            put("/api/mfa/challenge")
                .param("mfa-device-id", "A_WELL_DEFINED_MFA_DEVICE_ID")
                .principal(principalWithValidScopes)
        ).andExpect(status().isOk)
    }

    @Test
    fun `when an mfa challenge fails for insufficient scopes`() {
        val mfaDeviceId = MfaDeviceId("A_WELL_DEFINED_MFA_DEVICE_ID")
        every { mfaChallengeSender.sendMfaChallengeFor(account.email, mfaDeviceId) } just runs

        mokMvc.perform(
            put("/api/mfa/challenge")
                .param("mfa-device-id", "A_WELL_DEFINED_MFA_DEVICE_ID")
                .principal(principalWithoutValidScopes)
        ).andExpect(status().isForbidden)
    }
}
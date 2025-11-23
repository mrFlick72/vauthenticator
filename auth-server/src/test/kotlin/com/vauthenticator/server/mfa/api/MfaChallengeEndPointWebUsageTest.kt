package com.vauthenticator.server.mfa.api

import com.vauthenticator.server.mfa.domain.MfaChallengeSender
import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.SecurityFixture.principalFor
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
import java.util.*


@ExtendWith(MockKExtension::class)
class MfaChallengeEndPointWebUsageTest {
    lateinit var mokMvc: MockMvc

    private val account = AccountTestFixture.anAccount()
    private val clientAppId = aClientAppId()
    private val aClientApp = aClientApp(clientAppId).copy(scopes = Scopes.from(Scope.MFA_ALWAYS))
    private val forbiddenClientAppId = aClientAppId()
    private val forbiddenClientApp = aClientApp(forbiddenClientAppId)

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

    private val principal = principalFor(account.email)

    @Test
    fun `when an mfa challenge is sent to the default mfa device`() {
        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(aClientApp)
        every { mfaChallengeSender.sendMfaChallengeFor(account.email) } just runs

        mokMvc.perform(
            put("/api/mfa/challenge")
                .sessionAttr("clientId", clientAppId.content)
                .principal(principal)
        ).andExpect(status().isOk)
    }

    @Test
    fun `when an mfa challenge is sent to a specific mfa device`() {
        val mfaDeviceId = MfaDeviceId("A_WELL_DEFINED_MFA_DEVICE_ID")

        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(aClientApp)
        every { mfaChallengeSender.sendMfaChallengeFor(account.email, mfaDeviceId) } just runs

        mokMvc.perform(
            put("/api/mfa/challenge")
                .sessionAttr("clientId", clientAppId.content)
                .param("mfa-device-id", "A_WELL_DEFINED_MFA_DEVICE_ID")
                .principal(principal)
        ).andExpect(status().isOk)
    }

    @Test
    fun `when an mfa challenge fails for insufficient scopes`() {
        val mfaDeviceId = MfaDeviceId("A_WELL_DEFINED_MFA_DEVICE_ID")
        every { clientApplicationRepository.findOne(forbiddenClientAppId) } returns Optional.of(forbiddenClientApp)
        every { mfaChallengeSender.sendMfaChallengeFor(account.email, mfaDeviceId) } just runs

        mokMvc.perform(
            put("/api/mfa/challenge")
                .sessionAttr("clientId", forbiddenClientAppId.content)
                .param("mfa-device-id", "A_WELL_DEFINED_MFA_DEVICE_ID")
                .principal(principal)
        ).andExpect(status().isForbidden)
    }
}
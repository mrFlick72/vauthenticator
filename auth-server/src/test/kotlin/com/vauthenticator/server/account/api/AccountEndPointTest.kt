package com.vauthenticator.server.account.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.Phone
import com.vauthenticator.server.account.domain.SaveAccount
import com.vauthenticator.server.account.domain.signup.SignUpUse
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.ClientAppFixture
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

private const val ENDPOINT = "/api/accounts"

@ExtendWith(MockKExtension::class)
internal class AccountEndPointTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var signUpUse: SignUpUse

    @MockK
    lateinit var accountRepository: AccountRepository

    private val objectMapper = ObjectMapper()

    private val representation = AccountRepresentation(
        email = EMAIL,
        password = "secret",
        firstName = "A First Name",
        lastName = "A Last Name",
        authorities = emptyList(),
        birthDate = "",
        phone = Phone.nullValue().formattedPhone()
    )

    private val masterAccount = anAccount().copy(
        accountNonExpired = true,
        emailVerified = false,
        accountNonLocked = false,
        credentialsNonExpired = true,
        enabled = false,
    )

    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(
            AccountEndPoint(
                PermissionValidator(clientApplicationRepository),
                signUpUse,
                SaveAccount(accountRepository)
            )
        )
            .build()
    }

    @Test
    internal fun `sign up a new account`() {
        val clientAppId = ClientAppId(A_CLIENT_APP_ID)
        every { signUpUse.execute(clientAppId, masterAccount) } just runs
        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(
            ClientAppFixture.aClientApp(
                ClientAppId(A_CLIENT_APP_ID)
            )
        )

        mokMvc.perform(
            post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(
                    principalFor(
                        A_CLIENT_APP_ID,
                        EMAIL,
                        listOf(VAUTHENTICATOR_ADMIN),
                        listOf(Scope.SIGN_UP.content)
                    )
                )
                .content(objectMapper.writeValueAsString(representation))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)

        verify { signUpUse.execute(clientAppId, masterAccount) }

        masterAccountAssertions()
    }

    @Test
    internal fun `sign up a new account like form ui with client app id in the session`() {
        val clientAppId = ClientAppId(A_CLIENT_APP_ID)
        every { signUpUse.execute(clientAppId, masterAccount) } just runs
        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(
            ClientAppFixture.aClientApp(
                ClientAppId(A_CLIENT_APP_ID),
            ).copy(scopes = Scopes.from(Scope.SIGN_UP))
        )

        mokMvc.perform(
            post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .sessionAttr("clientId", A_CLIENT_APP_ID)
                .content(objectMapper.writeValueAsString(representation))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)

        verify { signUpUse.execute(clientAppId, masterAccount) }
        masterAccountAssertions()
    }

    @Test
    internal fun `update account details`() {
        val masterAccount = masterAccount.copy(
            accountNonExpired = true,
            emailVerified = true,
            accountNonLocked = true,
            credentialsNonExpired = true,
            enabled = true,
        )
        val clientAppId = A_CLIENT_APP_ID

        every { accountRepository.accountFor(EMAIL) } returns Optional.of(masterAccount)
        every { accountRepository.save(masterAccount) } just runs

        mokMvc.perform(
            MockMvcRequestBuilders.put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(principalFor(clientAppId, EMAIL, listOf(VAUTHENTICATOR_ADMIN)))
                .content(objectMapper.writeValueAsString(representation))
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        verify { accountRepository.save(masterAccount) }
    }

    private fun masterAccountAssertions() {
        assertEquals(false, masterAccount.accountNonLocked)
        assertEquals(false, masterAccount.emailVerified)
        assertEquals(true, masterAccount.accountNonExpired)
        assertEquals(true, masterAccount.credentialsNonExpired)
        assertEquals(false, masterAccount.enabled)
    }
}

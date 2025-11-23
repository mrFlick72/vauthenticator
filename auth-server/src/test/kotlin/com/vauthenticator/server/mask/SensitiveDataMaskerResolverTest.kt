package com.vauthenticator.server.mask

import com.vauthenticator.server.mfa.domain.MfaMethod
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SensitiveDataMaskerResolverTest {

    @MockK
    lateinit var emailMasker: SensitiveEmailMasker

    @MockK
    lateinit var phoneMasker: SensitivePhoneMasker

    lateinit var uut: SensitiveDataMaskerResolver

    @BeforeEach
    fun setUp() {
        uut = SensitiveDataMaskerResolver(
            mapOf(
                MfaMethod.EMAIL_MFA_METHOD to emailMasker,
                MfaMethod.SMS_MFA_METHOD to phoneMasker,
            )
        )
    }

    @Test
    fun `when an email marker is used`() {
        val actual = uut.getSensitiveDataMasker(MfaMethod.EMAIL_MFA_METHOD)
        assertTrue(actual is SensitiveEmailMasker)
    }

    @Test
    fun `when an phone marker is used`() {
        val actual = uut.getSensitiveDataMasker(MfaMethod.SMS_MFA_METHOD)
        assertTrue(actual is SensitivePhoneMasker)
    }
}
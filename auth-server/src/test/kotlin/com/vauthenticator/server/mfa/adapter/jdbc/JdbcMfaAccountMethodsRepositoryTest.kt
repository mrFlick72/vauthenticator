package com.vauthenticator.server.mfa.adapter.jdbc

import com.vauthenticator.server.mfa.adapter.AbstractMfaAccountMethodsRepositoryTest
import com.vauthenticator.server.mfa.domain.MfaDeviceRevocationCheck
import com.vauthenticator.server.mfa.domain.MfaDeviceRevocationException
import com.vauthenticator.server.mfa.domain.MfaDeviceRevocationRefusal
import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

@ExtendWith(MockKExtension::class)
class JdbcMfaAccountMethodsRepositoryTest : AbstractMfaAccountMethodsRepositoryTest() {
    override fun initMfaAccountMethodsRepository() = JdbcMfaAccountMethodsRepository(
        jdbcTemplate,
        keyRepository,
        masterKid,
        mfaDeviceIdGenerator
    )


    override fun resetDatabase() {
        resetDb()
    }

    @Test
    fun `when two mfa devices of an account are revoked concurrently the second revocation sees the first`() {
        val first = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val second = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, "another-$email", true)
        val transaction = TransactionTemplate(DataSourceTransactionManager(jdbcTemplate.dataSource!!))
        val keepsAnAssociatedDevice: MfaDeviceRevocationCheck = { candidate ->
            if (candidate.accountDevices.count { it.associated } <= 1) {
                throw MfaDeviceRevocationException(MfaDeviceRevocationRefusal.LAST_ASSOCIATED_DEVICE)
            }
        }
        val firstChecked = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()

        val firstRevocation = executor.submit {
            transaction.executeWithoutResult {
                uut.revoke(email, first.mfaDeviceId) { candidate ->
                    keepsAnAssociatedDevice(candidate)
                    firstChecked.countDown()
                    // keep the transaction open while the second revocation runs
                    Thread.sleep(1000)
                }
            }
        }
        firstChecked.await(5, SECONDS)

        assertThrows(MfaDeviceRevocationException::class.java) {
            transaction.executeWithoutResult { uut.revoke(email, second.mfaDeviceId, keepsAnAssociatedDevice) }
        }
        firstRevocation.get(5, SECONDS)
        executor.shutdown()

        assertEquals(listOf(second), uut.findAll(email))
    }
}

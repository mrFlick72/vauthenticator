package com.vauthenticator.server.mfa.adapter.jdbc

import com.vauthenticator.server.mfa.adapter.AbstractMfaAccountMethodsRepositoryTest
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class JdbcMfaAccountMethodsRepositoryTest : AbstractMfaAccountMethodsRepositoryTest() {
    override fun initMfaAccountMethodsRepository() = JdbcMfaAccountMethodsRepository(
        jdbcTemplate,
        keyRepository,
        masterKid
    ) { mfaDeviceId }


    override fun resetDatabase() {
        resetDb()
    }

}
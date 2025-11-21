package com.vauthenticator.server.management

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.management.cleanup.DatabaseTtlEntryCleanJob
import com.vauthenticator.server.management.cleanup.DatabaseTtlEntryCleanJobEndPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import com.vauthenticator.server.management.init.*
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.RoleRepository
import org.springframework.beans.factory.annotation.Value
import java.time.Clock

@Configuration(proxyBeanMethods = false)
class TenantInitConfig {

    @Bean
    fun accountSetUpJob(
        roleRepository: RoleRepository,
        accountRepository: AccountRepository,
        passwordEncoder: VAuthenticatorPasswordEncoder
    ) = AccountSetUpJob(
        roleRepository, accountRepository, passwordEncoder
    )

    @Bean
    fun clientApplicationSetUpJob(
        clientApplicationRepository: ClientApplicationRepository,
        passwordEncoder: VAuthenticatorPasswordEncoder

    ) = ClientApplicationSetUpJob(clientApplicationRepository, passwordEncoder)

    @Bean
    fun keySetUpJob(
        @Value("\${key.master-key.id}") maserKid: String,
        keyStorage: KeyStorage,
        keyRepository: KeyRepository
    ) = KeySetUpJob(
        maserKid, keyStorage, keyRepository
    )

    @Bean
    fun tenantSetUpEndPoint(
        accountSetUpJob: AccountSetUpJob,
        clientApplicationSetUpJob: ClientApplicationSetUpJob,
        keySetUpJob: KeySetUpJob
    ) =
        TenantSetUpEndPoint(
            accountSetUpJob,
            clientApplicationSetUpJob,
            keySetUpJob
        )

}

@Profile("database")
@Configuration(proxyBeanMethods = false)
class DatabaseTtlEntryCleanJobConfig {

    @Bean
    fun databaseTtlEntryCleanJob(
        jdbcTemplate: JdbcTemplate
    ) = DatabaseTtlEntryCleanJob(jdbcTemplate, Clock.systemUTC())

    @Bean
    fun databaseTtlEntryCleanJobEndPoint(databaseTtlEntryCleanJob: DatabaseTtlEntryCleanJob) =
        DatabaseTtlEntryCleanJobEndPoint(databaseTtlEntryCleanJob)
}
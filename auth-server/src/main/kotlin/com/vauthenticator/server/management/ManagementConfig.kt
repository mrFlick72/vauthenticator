package com.vauthenticator.server.management

import com.vauthenticator.server.account.domain.AccountAdministration
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.signup.SignUpUse
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.management.cleanup.DatabaseTtlEntryCleanJob
import com.vauthenticator.server.management.cleanup.DatabaseTtlEntryCleanJobEndPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import com.vauthenticator.server.management.init.*
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.Administration
import com.vauthenticator.server.role.domain.RoleRepository
import org.springframework.beans.factory.annotation.Value
import java.time.Clock

/**
 * Tenant setup creates the first Administrators, with no caller (ADR 0017). It is private to this file, so only
 * TenantInitConfig can use it; `AdministrationArchitectureTest` fails the build if anything else decides who is an
 * Administrator.
 */
private object TenantSetUpAdministration : Administration {
    override fun requireAdministrator() {}
}

@Configuration(proxyBeanMethods = false)
class TenantInitConfig {

    // Tenant setup creates the first Administrators with no caller: its SignUpUse is the one wired on
    // TenantSetUpAdministration (ADR 0017). Keep it inside this config.
    @Bean
    fun accountSetUpJob(
        roleRepository: RoleRepository,
        passwordPolicy: PasswordPolicy,
        accountRepository: AccountRepository,
        passwordEncoder: VAuthenticatorPasswordEncoder,
        eventsDispatcher: VAuthenticatorEventsDispatcher
    ) = AccountSetUpJob(
        roleRepository,
        SignUpUse(passwordPolicy, accountRepository, AccountAdministration(TenantSetUpAdministration), passwordEncoder, eventsDispatcher)
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

    @Bean
    fun databaseTtlEntryCleanJob(
        jdbcTemplate: JdbcTemplate
    ) = DatabaseTtlEntryCleanJob(jdbcTemplate, Clock.systemUTC())

    @Bean
    fun databaseTtlEntryCleanJobEndPoint(databaseTtlEntryCleanJob: DatabaseTtlEntryCleanJob) =
        DatabaseTtlEntryCleanJobEndPoint(databaseTtlEntryCleanJob)
}
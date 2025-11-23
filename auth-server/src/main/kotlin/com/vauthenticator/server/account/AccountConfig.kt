package com.vauthenticator.server.account

import com.fasterxml.jackson.databind.ObjectMapper
import com.hubspot.jinjava.Jinjava
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.account.adapter.CachedAccountRepository
import com.vauthenticator.server.account.adapter.dynamodb.DynamoDbAccountRepository
import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.AccountUpdateAdminAction
import com.vauthenticator.server.account.domain.SaveAccount
import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallenge
import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallengeUponSignUpEventConsumer
import com.vauthenticator.server.account.domain.emailverification.VerifyEMailChallenge
import com.vauthenticator.server.account.domain.signup.SignUpUse
import com.vauthenticator.server.account.domain.welcome.SayWelcome
import com.vauthenticator.server.account.domain.welcome.SendWelcomeMailUponSignUpEventConsumer
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.cache.RedisCacheOperation
import com.vauthenticator.server.communication.NoReplyEMailConfiguration
import com.vauthenticator.server.communication.adapter.JinJavaTemplateResolver
import com.vauthenticator.server.communication.adapter.javamail.JavaEMailSenderService
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.communication.domain.SimpleEMailMessageFactory
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollment
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollmentAssociation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mail.javamail.JavaMailSender
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class AccountConfig {

    @Bean
    fun changeAccountEnabling(accountRepository: AccountRepository): AccountUpdateAdminAction =
        AccountUpdateAdminAction(accountRepository)

    @Bean
    fun saveAccount(accountRepository: AccountRepository): SaveAccount =
        SaveAccount(accountRepository)


    @Bean("accountRepository")
    @Profile("database")
    fun jdbcAccountRepository(
        jdbcTemplate: JdbcTemplate
    ) = JdbcAccountRepository(jdbcTemplate)


    @Bean("accountRepository")
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.account.cache.enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    @Profile("dynamo")
    fun dynamoDbAccountRepository(
        mapper: ObjectMapper,
        dynamoDbClient: DynamoDbClient,
        roleRepository: RoleRepository,
        @Value("\${vauthenticator.dynamo-db.account.table-name}") accountTableName: String
    ) =
        DynamoDbAccountRepository(dynamoDbClient, accountTableName, roleRepository)


    @Bean("accountRepository")
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.account.cache.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile("dynamo")
    fun cachedDynamoDbAccountRepository(
        mapper: ObjectMapper,
        dynamoDbClient: DynamoDbClient,
        accountCacheOperation: CacheOperation<String, String>,
        roleRepository: RoleRepository,
        @Value("\${vauthenticator.dynamo-db.account.table-name}") accountTableName: String
    ) =
        CachedAccountRepository(
            AccountCacheContentConverter(mapper),
            accountCacheOperation,
            DynamoDbAccountRepository(dynamoDbClient, accountTableName, roleRepository),
        )

    @Bean
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.account.cache.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile("dynamo")
    fun accountCacheOperation(
        redisTemplate: RedisTemplate<*, *>,
        @Value("\${vauthenticator.dynamo-db.account.cache.ttl}") ttl: Duration,
        @Value("\${vauthenticator.dynamo-db.account.cache.name}") cacheRegionName: String,
    ) = RedisCacheOperation<String, String>(
        cacheName = cacheRegionName,
        ttl = ttl,
        redisTemplate = redisTemplate as RedisTemplate<String, String>
    )
}


@Configuration(proxyBeanMethods = false)
class WelcomeConfig {

    @Bean
    fun sayWelcome(
        accountRepository: AccountRepository,
        welcomeMailSender: EMailSenderService
    ) = SayWelcome(accountRepository, welcomeMailSender)

    @Bean
    fun welcomeMailSender(
        javaMailSender: JavaMailSender,
        documentRepository: DocumentRepository,
        noReplyEMailConfiguration: NoReplyEMailConfiguration
    ) =
        JavaEMailSenderService(
            documentRepository,
            javaMailSender,
            JinJavaTemplateResolver(Jinjava()),
            SimpleEMailMessageFactory(
                noReplyEMailConfiguration.from,
                noReplyEMailConfiguration.welcomeEMailSubject,
                EMailType.WELCOME
            )
        )

    @Bean
    fun sendWelcomeMailUponSignUpEventConsumer(sayWelcome: SayWelcome) =
        SendWelcomeMailUponSignUpEventConsumer(sayWelcome)
}


@Configuration(proxyBeanMethods = false)
class EMailVerificationConfig {

    @Bean
    fun sendVerifyMailChallenge(
        clientAccountRepository: ClientApplicationRepository,
        accountRepository: AccountRepository,
        mfaMethodsEnrollment: MfaMethodsEnrollment,
        verificationMailSender: EMailSenderService,
        @Value("\${vauthenticator.host}") frontChannelBaseUrl: String
    ) =
        SendVerifyEMailChallenge(
            accountRepository,
            mfaMethodsEnrollment,
            verificationMailSender,
            frontChannelBaseUrl
        )

    @Bean
    fun verifyMailChallengeSent(
        accountRepository: AccountRepository,
        ticketRepository: TicketRepository,
        mfaMethodsEnrollmentAssociation: MfaMethodsEnrollmentAssociation
    ) =
        VerifyEMailChallenge(
            ticketRepository,
            accountRepository,
            mfaMethodsEnrollmentAssociation
        )

    @Bean
    fun verificationMailSender(
        javaMailSender: JavaMailSender,
        documentRepository: DocumentRepository,
        noReplyEMailConfiguration: NoReplyEMailConfiguration
    ) =
        JavaEMailSenderService(
            documentRepository,
            javaMailSender,
            JinJavaTemplateResolver(Jinjava()),
            SimpleEMailMessageFactory(
                noReplyEMailConfiguration.from,
                noReplyEMailConfiguration.welcomeEMailSubject,
                EMailType.EMAIL_VERIFICATION
            )
        )

    @Bean
    fun sendVerifyMailChallengeUponSignUpEventConsumer(mailChallenge: SendVerifyEMailChallenge) =
        SendVerifyEMailChallengeUponSignUpEventConsumer(mailChallenge)
}


@Configuration(proxyBeanMethods = false)
class SingUpConfig {

    @Bean
    fun signUpUseCase(
        passwordPolicy: PasswordPolicy,
        clientAccountRepository: ClientApplicationRepository,
        accountRepository: AccountRepository,
        vAuthenticatorPasswordEncoder: VAuthenticatorPasswordEncoder,
        vAuthenticatorEventsDispatcher : VAuthenticatorEventsDispatcher
    ): SignUpUse =
        SignUpUse(
            passwordPolicy,
            accountRepository,
            vAuthenticatorPasswordEncoder,
            vAuthenticatorEventsDispatcher
        )

}
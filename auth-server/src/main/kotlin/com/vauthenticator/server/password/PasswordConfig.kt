package com.vauthenticator.server.password

import com.hubspot.jinjava.Jinjava
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.NoReplyEMailConfiguration
import com.vauthenticator.server.communication.adapter.JinJavaTemplateResolver
import com.vauthenticator.server.communication.adapter.javamail.JavaEMailSenderService
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.communication.domain.SimpleEMailMessageFactory
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.adapter.dynamodb.DynamoPasswordHistoryRepository
import com.vauthenticator.server.password.adapter.jdbc.JdbcPasswordHistoryRepository
import com.vauthenticator.server.password.domain.*
import com.vauthenticator.server.password.domain.changepassword.ChangePassword
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordEventConsumer
import com.vauthenticator.server.password.domain.resetpassword.ResetAccountPassword
import com.vauthenticator.server.password.domain.resetpassword.ResetPasswordEventConsumer
import com.vauthenticator.server.password.domain.resetpassword.SendResetPasswordMailChallenge
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mail.javamail.JavaMailSender
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Clock

@EnableConfigurationProperties(PasswordGeneratorCriteria::class, PasswordPolicyConfigProp::class)
@Configuration(proxyBeanMethods = false)
class PasswordConfig {

    @Bean
    fun changePasswordEventConsumer(passwordHistoryRepository: PasswordHistoryRepository) =
        ChangePasswordEventConsumer(passwordHistoryRepository)

    @Bean
    fun resetPasswordEventConsumer(passwordHistoryRepository: PasswordHistoryRepository) =
        ResetPasswordEventConsumer(passwordHistoryRepository)

    @Bean
    fun passwordPolicy(
        reusePreventionPasswordPolicy: PasswordPolicy,
        passwordPolicyConfigProp: PasswordPolicyConfigProp
    ): CompositePasswordPolicy {
        val passwordPolicies = mutableSetOf(
            MinimumCharacterPasswordPolicy(passwordPolicyConfigProp.minSize),
            SpecialCharacterPasswordPolicy(passwordPolicyConfigProp.minSpecialSymbol)
        )
        if (passwordPolicyConfigProp.enablePasswordReusePrevention) {
            passwordPolicies.add(reusePreventionPasswordPolicy)
        }

        return CompositePasswordPolicy(
            passwordPolicies
        )
    }

    @Bean
    fun reusePreventionPasswordPolicy(
        passwordEncoder: VAuthenticatorPasswordEncoder,
        passwordHistoryRepository: PasswordHistoryRepository
    ) = ReusePreventionPasswordPolicy(
        passwordEncoder,
        passwordHistoryRepository
    )

    @Profile("database")
    @Bean("passwordHistoryRepository")
    fun jdbcPasswordHistoryRepository(
        @Value("\${password.password-history.history-evaluation-limit}") historyEvaluationLimit: Int,
        @Value("\${password.password-history.max-history-allowed-size}") maxHistoryAllowedSize: Int,
        jdbcTemplate: JdbcTemplate
    ): JdbcPasswordHistoryRepository = JdbcPasswordHistoryRepository(
        historyEvaluationLimit,
        maxHistoryAllowedSize,
        Clock.systemUTC(),
        jdbcTemplate
    )

    @Profile("dynamo")
    @Bean("passwordHistoryRepository")
    fun dynamoDbPasswordHistoryRepository(
        @Value("\${password.password-history.history-evaluation-limit}") historyEvaluationLimit: Int,
        @Value("\${password.password-history.max-history-allowed-size}") maxHistoryAllowedSize: Int,
        @Value("\${vauthenticator.dynamo-db.password-history.table-name}") dynamoPasswordHistoryTableName: String,
        dynamoDbClient: DynamoDbClient
    ): DynamoPasswordHistoryRepository = DynamoPasswordHistoryRepository(
        historyEvaluationLimit,
        maxHistoryAllowedSize,
        Clock.systemUTC(),
        dynamoPasswordHistoryTableName,
        dynamoDbClient
    )

    @Bean
    fun changePassword(
        eventsDispatcher: VAuthenticatorEventsDispatcher,
        passwordPolicy: PasswordPolicy,
        passwordEncoder: VAuthenticatorPasswordEncoder,
        accountRepository: AccountRepository
    ) =
        ChangePassword(eventsDispatcher, passwordPolicy, passwordEncoder, accountRepository)

    @Bean
    fun passwordGenerator(passwordGeneratorCriteria: PasswordGeneratorCriteria) =
        PasswordGenerator(passwordGeneratorCriteria)
}

@ConfigurationProperties(prefix = "password.policy")
data class PasswordPolicyConfigProp(
    val minSize: Int,
    val minSpecialSymbol: Int,
    val enablePasswordReusePrevention: Boolean
)


@Configuration(proxyBeanMethods = false)
class ResetPasswordConfig {

    @Bean
    fun sendResetPasswordMailChallenge(
        accountRepository: AccountRepository,
        clientApplicationRepository: ClientApplicationRepository,
        ticketCreator: TicketCreator,
        resetPasswordMailSender: EMailSenderService,
        @Value("\${vauthenticator.host}") frontChannelBaseUrl: String
    ) =
        SendResetPasswordMailChallenge(
            accountRepository,
            ticketCreator,
            resetPasswordMailSender,
            frontChannelBaseUrl
        )

    @Bean
    fun resetPasswordChallengeSent(
        eventsDispatcher: VAuthenticatorEventsDispatcher,
        accountRepository: AccountRepository,
        vAuthenticatorPasswordEncoder: VAuthenticatorPasswordEncoder,
        passwordPolicy: PasswordPolicy,
        ticketRepository: TicketRepository
    ) =
        ResetAccountPassword(
            eventsDispatcher,
            accountRepository,
            vAuthenticatorPasswordEncoder,
            passwordPolicy,
            ticketRepository
        )

    @Bean
    fun resetPasswordMailSender(
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
                noReplyEMailConfiguration.resetPasswordEMailSubject,
                EMailType.RESET_PASSWORD
            )
        )

}
package com.vauthenticator.server.mfa

import com.hubspot.jinjava.Jinjava
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.NoReplyEMailConfiguration
import com.vauthenticator.server.communication.adapter.JinJavaTemplateResolver
import com.vauthenticator.server.communication.adapter.javamail.JavaEMailSenderService
import com.vauthenticator.server.communication.adapter.sns.SnsSmsSenderService
import com.vauthenticator.server.communication.domain.*
import com.vauthenticator.server.keys.domain.KeyDecrypter
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.MasterKid
import com.vauthenticator.server.mask.SensitiveDataMaskerResolver
import com.vauthenticator.server.mask.SensitiveEmailMasker
import com.vauthenticator.server.mask.SensitivePhoneMasker
import com.vauthenticator.server.mfa.adapter.dynamodb.DynamoMfaAccountMethodsRepository
import com.vauthenticator.server.mfa.adapter.jdbc.JdbcMfaAccountMethodsRepository
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mail.javamail.JavaMailSender
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.sns.SnsClient
import java.util.*

@Configuration(proxyBeanMethods = false)
class MfaConfig {

    @Bean("mfaAccountMethodsRepository")
    @Profile("dynamo")
    fun dynamoDbMfaAccountMethodsRepository(
        keyRepository: KeyRepository,
        dynamoDbClient: DynamoDbClient,
        @Value("\${key.master-key.id}") masterKey: String,
        @Value("\${vauthenticator.dynamo-db.mfa-account-methods.table-name}") mfaAccountMethodTableName: String,
        @Value("\${vauthenticator.dynamo-db.default-mfa-account-methods.table-name}") defaultMfaAccountMethodTableName: String
    ): MfaAccountMethodsRepository =
        DynamoMfaAccountMethodsRepository(
            mfaAccountMethodTableName,
            defaultMfaAccountMethodTableName,
            dynamoDbClient,
            keyRepository,
            MasterKid(masterKey)
        ) { MfaDeviceId(UUID.randomUUID().toString()) }

    @Bean("mfaAccountMethodsRepository")
    @Profile("database")
    fun jdbcMfaAccountMethodsRepository(
        keyRepository: KeyRepository,
        jdbcTemplate: JdbcTemplate,
        @Value("\${key.master-key.id}") masterKey: String
    ): MfaAccountMethodsRepository =
        JdbcMfaAccountMethodsRepository(
            jdbcTemplate,
            keyRepository,
            MasterKid(masterKey)
        ) { MfaDeviceId(UUID.randomUUID().toString()) }

    @Bean
    fun sensitiveEmailMasker() = SensitiveEmailMasker()

    @Bean
    fun sensitivePhoneMasker() = SensitivePhoneMasker()

    @Bean
    fun sensitiveDataMaskerResolver(
        sensitiveEmailMasker: SensitiveEmailMasker,
        sensitivePhoneMasker: SensitivePhoneMasker
    ) = SensitiveDataMaskerResolver(
        mapOf(
            MfaMethod.EMAIL_MFA_METHOD to sensitiveEmailMasker,
            MfaMethod.SMS_MFA_METHOD to sensitivePhoneMasker
        )
    )

    @Bean
    fun mfaMethodsEnrolmentAssociation(
        ticketRepository: TicketRepository,
        mfaAccountMethodsRepository: MfaAccountMethodsRepository,
        mfaVerifier: MfaVerifier
    ) =
        MfaMethodsEnrollmentAssociation(ticketRepository, mfaAccountMethodsRepository, mfaVerifier)

    @Bean
    fun mfaMethodsEnrollment(
        mfaSender: MfaChallengeSender,
        ticketCreator: TicketCreator,
        accountRepository: AccountRepository,
        mfaAccountMethodsRepository: MfaAccountMethodsRepository,
        sensitiveDataMaskerResolver: SensitiveDataMaskerResolver
    ) = MfaMethodsEnrollment(
        accountRepository,
        ticketCreator,
        mfaSender,
        mfaAccountMethodsRepository,
        sensitiveDataMaskerResolver
    )

    @Bean
    fun otpMfa(
        keyRepository: KeyRepository,
        keyDecrypter: KeyDecrypter,
        mfaAccountMethodsRepository: MfaAccountMethodsRepository,
        otpConfigurationProperties: OtpConfigurationProperties
    ) = TaimosOtpMfa(
        keyDecrypter,
        keyRepository,
        mfaAccountMethodsRepository,
        otpConfigurationProperties
    )

    @Bean
    fun otpMfaSender(
        accountRepository: AccountRepository,
        otpMfa: OtpMfa,
        mfaMailSender: EMailSenderService,
        smsSenderService: SmsSenderService,
        mfaAccountMethodsRepository: MfaAccountMethodsRepository
    ) = MfaChallengeSender(accountRepository, otpMfa, mfaMailSender, smsSenderService, mfaAccountMethodsRepository)

    @Bean
    fun otpMfaVerifier(
        otpMfa: OtpMfa,
        accountRepository: AccountRepository,
        mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    ) = OtpMfaVerifier(accountRepository, otpMfa, mfaAccountMethodsRepository)

    @Bean
    fun mfaSmsSender(
        snsClient: SnsClient
    ) = SnsSmsSenderService(snsClient, SimpleSmsMessageFactory())

    @Bean
    fun mfaMailSender(
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
                noReplyEMailConfiguration.mfaEMailSubject,
                EMailType.MFA
            )
        )
}

@ConfigurationProperties("mfa.otp")
data class OtpConfigurationProperties(val length: Int, val timeToLiveInSeconds: Int)
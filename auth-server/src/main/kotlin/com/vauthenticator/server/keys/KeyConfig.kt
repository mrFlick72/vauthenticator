package com.vauthenticator.server.keys

import com.vauthenticator.server.keys.adapter.jdbc.JdbcKeyStorage
import com.vauthenticator.server.keys.adapter.kms.KmsKeyDecrypter
import com.vauthenticator.server.keys.adapter.kms.KmsKeyGenerator
import com.vauthenticator.server.keys.adapter.java.*
import com.vauthenticator.server.keys.domain.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import software.amazon.awssdk.services.kms.KmsClient
import java.time.Clock
import java.util.*

@Configuration(proxyBeanMethods = false)
class KeyConfig {

    @Profile("kms")
    @Bean("keyGenerator")
    fun kmsKeyGenerator(kmsClient: KmsClient): KeyGenerator = KmsKeyGenerator(kmsClient)

    @Profile("!kms")
    @Bean("keyGenerator")
    fun JavaSecurityKeyGenerator(
        kmsClient: KmsClient,
        storage: KeyGeneratorMasterKeyStorage
    ): KeyGenerator = JavaSecurityKeyGenerator(
        JavaSecurityCryptographicOperations(
            KeyGeneratorMasterKeyRepository(storage)
        )
    )

    @Profile("kms")
    @Bean("keyDecrypter")
    fun kmsKeyDecrypter(kmsClient: KmsClient): KeyDecrypter = KmsKeyDecrypter(kmsClient)

    @Profile("!kms")
    @Bean("keyDecrypter")
    fun JavaSecurityKeyDecrypter(
        @Value("\${key.master-key.id}") maserKid: String,
        storage: KeyGeneratorMasterKeyStorage
    ): KeyDecrypter = JavaSecurityKeyDecrypter(
        maserKid,
        JavaSecurityCryptographicOperations(
            KeyGeneratorMasterKeyRepository(storage)
        )
    )

    @Bean("keyStorage")
    fun jdbcKeyStorage(jdbcTemplate: JdbcTemplate, clock: Clock) = JdbcKeyStorage(jdbcTemplate, clock)

    @Bean("keyRepository")
    fun keyRepository(
        keyGenerator: KeyGenerator,
        keyDecrypter: KeyDecrypter,
        keyStorage: KeyStorage
    ): KeyRepository =
        KeyRepository(
            { UUID.randomUUID().toString() },
            keyStorage,
            keyGenerator,
        )

    @Bean
    fun signatureKeyRotation(keyRepository: KeyRepository) = SignatureKeyRotation(keyRepository)
}
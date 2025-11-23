package com.vauthenticator.server.keys.adapter.java

import com.vauthenticator.server.keys.domain.MasterKid
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


class KeyGeneratorMasterKeyRepository(
    val storage: KeyGeneratorMasterKeyStorage
) {

    fun maskerKeyFor(masterKeyId: MasterKid): String {
        return storage.content[masterKeyId.content()]!!
    }

}

@Profile("!kms")
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KeyGeneratorMasterKeyStorage::class)
class KeyGeneratorMasterKeyRepositoryConfig {

}

@ConfigurationProperties(prefix = "key.master-key.storage")
data class KeyGeneratorMasterKeyStorage(val content: Map<String, String>) {

}
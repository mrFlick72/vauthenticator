package it.valeriovaudi.vauthenticator.config

import it.valeriovaudi.vauthenticator.account.AccountRepository
import it.valeriovaudi.vauthenticator.account.JdbcAccountRepository
import it.valeriovaudi.vauthenticator.account.MongoAccountRepository
import it.valeriovaudi.vauthenticator.extentions.VAuthenticatorPasswordEncoder
import it.valeriovaudi.vauthenticator.keypair.KeyPairConfig
import it.valeriovaudi.vauthenticator.keypair.RestKeyRepository
import it.valeriovaudi.vauthenticator.oauth2.clientapp.JdbcClientApplicationRepository
import it.valeriovaudi.vauthenticator.openid.connect.userinfo.UserInfoFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.client.RestTemplate
import javax.sql.DataSource


@Configuration
class RepositoryConfig {

    @Bean
    fun userInfoFactory(accountRepository: AccountRepository) =
            UserInfoFactory(accountRepository)

    @Bean
    fun clientApplicationRepository(dataSource: DataSource, vAuthenticatorPasswordEncoder: VAuthenticatorPasswordEncoder) =
            JdbcClientApplicationRepository(JdbcTemplate(dataSource))

    @Bean
    @ConfigurationProperties(prefix = "key-store")
    fun keyPairConfig() = KeyPairConfig()

    @Bean("keyRepository")
    fun keyRepository(@Value("\${repository-service.baseUrl}") repositoryServiceUrl: String,
                      @Value("\${repository-service.serviceRegistrationName}") registrationName: String,
                      keyPairConfig: KeyPairConfig): RestKeyRepository =
            RestKeyRepository(RestTemplate(), repositoryServiceUrl, registrationName, keyPairConfig)

}

@Configuration
@ConditionalOnProperty(prefix = "account.repository", name = ["engine"], havingValue = "database")
@EnableAutoConfiguration(exclude = [
    MongoAutoConfiguration::class,
    MongoDataAutoConfiguration::class]
)
class AccountRepositoryWithDatabaseConfig {
    @Bean
    fun accountRepository(dataSource: DataSource) =
            JdbcAccountRepository(JdbcTemplate(dataSource))

}

@Configuration
@ConditionalOnProperty(prefix = "account.repository", name = ["engine"], havingValue = "mongo")
class AccountRepositoryWithMongoConfig {
    @Bean
    fun accountRepository(mongoTemplate: MongoTemplate) =
            MongoAccountRepository(mongoTemplate)

}
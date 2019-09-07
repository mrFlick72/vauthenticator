package it.valeriovaudi.vauthenticator.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import it.valeriovaudi.vauthenticator.account.Account
import it.valeriovaudi.vauthenticator.account.GetAccount
import it.valeriovaudi.vauthenticator.account.RabbitAccountRepository
import it.valeriovaudi.vauthenticator.account.RabbitMessageAccountAdapter
import it.valeriovaudi.vauthenticator.keypair.FileKeyRepository
import it.valeriovaudi.vauthenticator.keypair.KeyPairConfig
import it.valeriovaudi.vauthenticator.keypair.S3Config
import it.valeriovaudi.vauthenticator.keypair.S3KeyRepository
import it.valeriovaudi.vauthenticator.openidconnect.userinfo.UserInfoFactory
import it.valeriovaudi.vauthenticator.userdetails.AccountUserDetailsServiceAdapter
import it.valeriovaudi.vauthenticator.userdetails.NotParsableAccountDetails
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.MessageChannels

@Configuration
class RepositoryConfig {

    @Bean
    fun userInfoFactory(accountRepository: RabbitAccountRepository) =
            UserInfoFactory(accountRepository)

    @Bean
    fun accountRepository(getAccount: GetAccount) =
            RabbitAccountRepository(getAccount)

    @Bean
    @ConfigurationProperties(prefix = "key-store")
    fun keyPairConfig() = KeyPairConfig()

    @Bean
    @ConfigurationProperties(prefix = "key-store.aws.s3")
    fun s3Config() = S3Config()

    @Bean("keyRepository")
    @ConditionalOnProperty(value = ["vauthenticator.keypair.repository.type"], havingValue = "FILE_SYSTEM")
    fun fileKeyRepository() = FileKeyRepository(keyPairConfig())


    @Bean("keyRepository")
    @ConditionalOnProperty(value = ["vauthenticator.keypair.repository.type"], havingValue = "AWS_S3")
    fun s3KeyRepository(): S3KeyRepository {
        val s3Config = s3Config()
        val credentials = BasicAWSCredentials(s3Config.accessKey, s3Config.secretKey)

        val s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .withRegion(s3Config.region)
                .build()

        return S3KeyRepository(keyPairConfig(), s3Config, s3client)
    }

}

@Configuration
class AccountRepositoryConfig {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Bean("getAccountInboundQueue")
    fun getAccountInboundQueue() =
            Queue("getAccountInboundQueue", false)


    @Bean("getAccountInboundChannel")
    fun getAccountInboundChannel() =
            MessageChannels.direct().get()


    @Bean("getAccountOutboundChannel")
    fun getAccountOutboundChannel() =
            MessageChannels.direct().get()


    @Bean
    fun rabbitMessageAccountAdapter(objectMapper: ObjectMapper) =
            RabbitMessageAccountAdapter(objectMapper)

    @Bean
    fun getAccountPipelineConfig(accountUserDetailsServiceAdapter: AccountUserDetailsServiceAdapter,
                                 rabbitMessageAccountAdapter: RabbitMessageAccountAdapter,
                                 getAccountInboundChannel: DirectChannel,
                                 getAccountOutboundChannel: DirectChannel) =
            IntegrationFlows.from(getAccountInboundChannel)
                    .handle<AmqpOutboundEndpoint>(Amqp.outboundGateway(rabbitTemplate)
                            .routingKey("getAccountInboundQueue")
                            .returnChannel(getAccountOutboundChannel))
                    .transform(rabbitMessageAccountAdapter, "convert")
                    .get()
}
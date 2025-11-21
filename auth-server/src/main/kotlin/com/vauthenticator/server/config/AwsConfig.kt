package com.vauthenticator.server.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider
import software.amazon.awssdk.core.client.builder.SdkClientBuilder
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import java.net.URI
import java.util.*

@Configuration(proxyBeanMethods = false)
class AwsConfig {

    @Bean("awsCredentialsProvider")
    @ConditionalOnProperty("iamUserAwsCredentialsProvider", havingValue = "true")
    fun iamUserAwsCredentialsProvider(): AwsCredentialsProvider {
        return EnvironmentVariableCredentialsProvider.create()
    }


    @Bean("awsCredentialsProvider")
    @ConditionalOnProperty("iamServiceAccountAwsCredentialsProvider", havingValue = "true")
    fun iamServiceAccountAwsCredentialsProvider(): AwsCredentialsProvider {
        return WebIdentityTokenFileCredentialsProvider.builder()
            .roleSessionName("vauthenticator-${UUID.randomUUID()}")
            .build()
    }

    @Bean
    fun snsClient(
        awsCredentialsProvider: AwsCredentialsProvider,
        @Value("\${aws.sns.endpointOverride:}") endpointOverride: String
    ): SnsClient = SnsClient.builder().credentialsProvider(awsCredentialsProvider)
        .applyMutation { setEndPoint(endpointOverride, it) }
        .build()

    @Bean
    fun s3Client(
        awsCredentialsProvider: AwsCredentialsProvider,
        @Value("\${aws.s3.endpointOverride:}") endpointOverride: String
    ): S3Client = S3Client.builder().credentialsProvider(awsCredentialsProvider)
        .applyMutation { setEndPoint(endpointOverride, it) }
        .build()

    @Bean
    fun kmsClient(
        awsCredentialsProvider: AwsCredentialsProvider,
        @Value("\${aws.kms.endpointOverride:}") endpointOverride: String
    ) = KmsClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .applyMutation { setEndPoint(endpointOverride, it) }
        .build()


    @Bean
    fun dynamoDbClient(
        awsCredentialsProvider: AwsCredentialsProvider,
        @Value("\${aws.dynamodb.endpointOverride:}") endpointOverride: String
    ) = DynamoDbClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .applyMutation { setEndPoint(endpointOverride, it) }
        .build()

    @Bean
    fun lambdaClient(
        awsCredentialsProvider: AwsCredentialsProvider,
        @Value("\${aws.lambda.endpointOverride:}") endpointOverride: String
    ) = LambdaClient.builder()
        .credentialsProvider(awsCredentialsProvider)
        .applyMutation { setEndPoint(endpointOverride, it) }
        .build()

    private fun setEndPoint(
        endpointOverride: String,
        awsClient: SdkClientBuilder<*, *>
    ): SdkClientBuilder<*, *> {
        if (endpointOverride.isNotBlank()) {
            awsClient.endpointOverride(URI.create(endpointOverride))
        }
        return awsClient
    }

}
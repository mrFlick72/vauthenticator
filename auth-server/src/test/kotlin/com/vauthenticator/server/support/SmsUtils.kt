package com.vauthenticator.server.support

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.web.client.RestClient.builder
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import java.net.URI

private val objectMapper = jacksonObjectMapper()
private val restClient = builder().build()

object SmsUtils {

    val snsClient: SnsClient = SnsClient.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create("ACCESS_KEY_ID", "SECRET_ACCESS_KEY")
            )
        ).region(Region.US_EAST_1)
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()

    fun getMessageFor(phoneNumber: String, messageId: Int = 0): String {

        val body = restClient.get()
            .uri("http://localhost:4566/_aws/sns/sms-messages")
            .retrieve()
            .body(String::class.java)


        return objectMapper
            .readTree(body)
            .at("/sms_messages/$phoneNumber/$messageId/Message").asText()
    }

    fun resetSmsProvider() {
        restClient.delete()
            .uri("http://localhost:4566/_aws/sns/sms-messages")
            .retrieve()
    }
}
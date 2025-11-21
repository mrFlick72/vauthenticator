package com.vauthenticator.server.lambdas

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import software.amazon.awssdk.services.lambda.LambdaClient
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class LambdaConfig {

    @Bean
    fun lambdaFunction(
        redisTemplate: RedisTemplate<String, String>,
        @Value("\${vauthenticator.lambda.aws.function-result-cache-ttl:10s}") ttl: Duration,
        objectMapper: ObjectMapper,
        client: LambdaClient
    ) = AwsLambdaFunction(redisTemplate,ttl, objectMapper, client)
}
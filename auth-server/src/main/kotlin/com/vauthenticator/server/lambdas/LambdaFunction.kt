package com.vauthenticator.server.lambdas

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.extentions.toSha256
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import java.time.Duration
import java.util.*

interface LambdaFunction {

    fun save(
        name: LambdaFunctionName,
        content: LambdaFunctionContent,
        dependencies: LambdaFunctionDependencies
    ): LambdaFunctionId

    fun delete(id: LambdaFunctionId)

    fun execute(id: LambdaFunctionId, context: LambdaFunctionContext): LambdaFunctionContext

}

@JvmInline
value class LambdaFunctionId(val content: String)

typealias LambdaFunctionContent = ByteArray

@JvmInline
value class LambdaFunctionContext(val content: Map<String, Any>) {

    companion object {
        fun empty() = LambdaFunctionContext(
            mapOf(
                "user" to emptyMap(),
                "general_context_claims" to mapOf(
                    "client_id" to "",
                    "grant_flow" to "",
                    "authorized_scope" to emptySet<String>()
                ),
                "access_token_claims" to emptyMap(),
                "id_token_claims" to emptyMap()
            )
        )
    }
}

data class LambdaFunctionDependency(val name: String, val version: String)

typealias LambdaFunctionDependencies = List<LambdaFunctionDependency>

@JvmInline
value class LambdaFunctionName(val content: String)


fun interface LambdaFunctionContextFactory<T> {
    fun newLambdaFunctionContext(input: T): LambdaFunctionContext
}

class AwsLambdaFunction(
    private val redisTemplate: RedisTemplate<String, String>,
    private val ttl: Duration,
    private val objectMapper: ObjectMapper,
    private val client: LambdaClient
) : LambdaFunction {
    override fun save(
        name: LambdaFunctionName,
        content: LambdaFunctionContent,
        dependencies: LambdaFunctionDependencies
    ): LambdaFunctionId {
        TODO("Not yet implemented")
    }

    override fun delete(id: LambdaFunctionId) {
        TODO("Not yet implemented")
    }

    override fun execute(id: LambdaFunctionId, context: LambdaFunctionContext): LambdaFunctionContext {
        val typeRef: TypeReference<Map<String, Any>> = object : TypeReference<Map<String, Any>>() {}
        val sessionId = currentRequestAttributes().sessionId;

        val opsForHash = redisTemplate.opsForHash<String, String>()
        return Optional.ofNullable(opsForHash.get(sessionId, sessionId.toSha256()))
            .map { LambdaFunctionContext(objectMapper.readValue(it, typeRef)) }
            .orElseGet {
                val invokeRequest: InvokeRequest = InvokeRequest.builder()
                    .functionName(id.content)
                    .payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(context.content)))
                    .build()

                val invoke = client.invoke(invokeRequest)
                val serializedBody = invoke.payload().asUtf8String()

                opsForHash.put(sessionId, sessionId.toSha256(), serializedBody)
                opsForHash.operations.expire(sessionId, ttl)
                LambdaFunctionContext(objectMapper.readValue(serializedBody, typeRef))
            }
    }

}
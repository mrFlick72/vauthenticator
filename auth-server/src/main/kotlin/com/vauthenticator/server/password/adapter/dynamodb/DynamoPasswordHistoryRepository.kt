package com.vauthenticator.server.password.adapter.dynamodb

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

class DynamoPasswordHistoryRepository(
    private val historyEvaluationLimit: Int,
    private val maxHistoryAllowedSize: Int,
    private val clock: Clock,
    private val dynamoPasswordHistoryTableName: String,
    private val dynamoDbClient: DynamoDbClient
) : PasswordHistoryRepository {
    override fun store(userName: String, password: Password) {
        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(dynamoPasswordHistoryTableName)
                .item(
                    mapOf(
                        "user_name" to userName.asDynamoAttribute(),
                        "created_at" to createdAt().asDynamoAttribute(),
                        "password" to password.content.asDynamoAttribute()
                    )
                )
                .build()
        )
    }

    private fun createdAt() =
        LocalDateTime.now(clock)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

    override fun load(userName: String): List<Password> {
        val items = dynamoDbClient.query(
            QueryRequest.builder()
                .tableName(dynamoPasswordHistoryTableName)
                .scanIndexForward(false)
                .keyConditionExpression("user_name=:email")
                .expressionAttributeValues(mapOf(":email" to userName.asDynamoAttribute())).build()
        ).items()

        val allowedPassword = items.take(historyEvaluationLimit)

        deleteUselessPasswordHistory(items)
        return allowedPassword.map { Password(it.valueAsStringFor("password")) }
    }

    private fun deleteUselessPasswordHistory(itemsInTheHistory: List<Map<String, AttributeValue>>) {
        val leftoverSize = itemsInTheHistory.size - maxHistoryAllowedSize
        if (leftoverSize > 0) {
            itemsInTheHistory.takeLast(leftoverSize)
                .forEach { itemToDelete ->
                    dynamoDbClient.deleteItem(
                        DeleteItemRequest.builder()
                            .tableName(dynamoPasswordHistoryTableName)
                            .key(itemToDelete.filterKeys { it != "password" })
                            .build()
                    )
                }
        }
    }
}
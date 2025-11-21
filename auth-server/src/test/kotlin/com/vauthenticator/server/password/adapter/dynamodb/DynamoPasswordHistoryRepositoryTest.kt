package com.vauthenticator.server.password.adapter.dynamodb

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.password.adapter.AbstractPasswordHistoryRepositoryTest
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import com.vauthenticator.server.support.DynamoDbUtils
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoPasswordHistoryTableName
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.time.Clock


@ExtendWith(MockKExtension::class)
class DynamoPasswordHistoryRepositoryTest : AbstractPasswordHistoryRepositoryTest() {

    override fun initPasswordHistoryRepository(): PasswordHistoryRepository = DynamoPasswordHistoryRepository(
        2,
        3,
        Clock.systemUTC(),
        dynamoPasswordHistoryTableName,
        dynamoDbClient
    )

    override fun resetDatabase() {
        DynamoDbUtils.resetDynamoDb()
    }


    override fun loadActualDynamoSizeFor(userName: String): List<Map<String, Any>> {
        return dynamoDbClient.query(
            QueryRequest.builder()
                .tableName(dynamoPasswordHistoryTableName)
                .scanIndexForward(false)
                .keyConditionExpression("user_name=:email")
                .expressionAttributeValues(mapOf(":email" to userName.asDynamoAttribute())).build()
        ).items()
    }

}
package com.vauthenticator.server.account.adapter.dynamodb

import com.vauthenticator.server.account.adapter.dynamodb.DynamoAccountConverter.fromDomainToDynamo
import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.extentions.asDynamoAttribute
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

object DynamoAccountQueryFactory {

    fun findAccountQueryForUserName(username: String, table: String): GetItemRequest = GetItemRequest.builder()
        .tableName(table)
        .key(
            mutableMapOf(
                "user_name" to username.asDynamoAttribute()
            )
        )
        .build()

    fun storeAccountQueryFor(account: Account, table: String, withUpsert: Boolean = true): PutItemRequest =
        PutItemRequest.builder()
            .tableName(table)
            .item(fromDomainToDynamo(account))
            .let {
                if (withUpsert)
                    it
                else
                    it.conditionExpression("user_name <> :username")
                        .expressionAttributeValues(mutableMapOf(":username" to account.username.asDynamoAttribute()))
            }
            .build()

}
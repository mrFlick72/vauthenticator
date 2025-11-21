package com.vauthenticator.server.role.adapter.dynamodb

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.role.domain.Role
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest

object DynamoDbRoleMapper {
    fun findAllRequestFor(tableName: String): ScanRequest =
        ScanRequest.builder()
            .tableName(tableName)
            .build()

    fun saveRoleRequestFor(role: Role, tableName: String): PutItemRequest = PutItemRequest.builder()
        .tableName(tableName)
        .item(
            mutableMapOf(
                "role_name" to role.name.asDynamoAttribute(),
                "description" to role.description.asDynamoAttribute()
            )
        )
        .build()


    fun deleteRoleRequestFor(roleName: String, tableName: String): DeleteItemRequest = DeleteItemRequest.builder()
        .tableName(tableName)
        .key(
            mutableMapOf(
                "role_name" to roleName.asDynamoAttribute()
            )
        )
        .build()

    fun roleFor(it: MutableMap<String, AttributeValue>) =
        Role(
            it.valueAsStringFor("role_name"),
            it.valueAsStringFor("description", "")
        )

}
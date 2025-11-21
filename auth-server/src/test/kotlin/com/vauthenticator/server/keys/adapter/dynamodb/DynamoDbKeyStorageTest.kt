package com.vauthenticator.server.keys.adapter.dynamodb

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.valueAsBoolFor
import com.vauthenticator.server.extentions.valueAsLongFor
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.keys.adapter.AbstractKeyStorageTest
import com.vauthenticator.server.keys.adapter.dynamo.DynamoDbKeyStorage
import com.vauthenticator.server.keys.domain.KeyPurpose
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoMfaKeysTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoSignatureKeysTableName
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest

class DynamoDbKeyStorageTest : AbstractKeyStorageTest() {

    override fun initKeyStorage(): KeyStorage = DynamoDbKeyStorage(
        clock(),
        dynamoDbClient,
        dynamoSignatureKeysTableName,
        dynamoMfaKeysTableName
    )

    override fun resetDatabase() {
        resetDynamoDb()
    }

    override fun getActual(kid: Kid, keyPurpose: KeyPurpose): Map<String, Any> {
        val items = dynamoDbClient.getItem(
            GetItemRequest.builder().tableName(tableNameFor(keyPurpose))
                .key(
                    mapOf(
                        "key_id" to kid.content().asDynamoAttribute()
                    )
                )
                .build()
        ).item()

        return if(items.isNotEmpty()) {
            mapOf(
                "key_id" to items.valueAsStringFor("key_id"),
                "master_key_id" to items.valueAsStringFor("master_key_id"),
                "encrypted_private_key" to items.valueAsStringFor("encrypted_private_key"),
                "public_key" to items.valueAsStringFor("public_key"),
                "key_expiration_date_timestamp" to items.valueAsLongFor("key_expiration_date_timestamp"),
                "enabled" to items.valueAsBoolFor("enabled"),
            )
        }else {
            emptyMap()
        }
    }


    private fun tableNameFor(keyPurpose: KeyPurpose) = when (keyPurpose) {
        KeyPurpose.MFA -> dynamoMfaKeysTableName
        KeyPurpose.SIGNATURE -> dynamoSignatureKeysTableName
    }
}
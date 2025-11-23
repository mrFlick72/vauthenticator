package com.vauthenticator.server.keys.adapter.dynamo

import com.vauthenticator.server.extentions.*
import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.keys.domain.KeyType
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.time.Clock
import java.time.Duration

class DynamoDbKeyStorage(
    private val clock: Clock,
    private val dynamoDbClient: DynamoDbClient,
    private val signatureTableName: String,
    private val mfaTableName: String,
) : KeyStorage {

    private val logger = LoggerFactory.getLogger(DynamoDbKeyStorage::class.java)

    override fun store(
        masterKid: MasterKid,
        kid: Kid,
        dataKey: DataKey,
        keyType: KeyType,
        keyPurpose: KeyPurpose
    ) {
        val tableName = tableNameBasedOn(keyPurpose)
        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        "master_key_id" to masterKid.content().asDynamoAttribute(),
                        "key_id" to kid.content().asDynamoAttribute(),
                        "encrypted_private_key" to dataKey.encryptedPrivateKeyAsString().asDynamoAttribute(),
                        "public_key" to dataKey.publicKeyAsString().asDynamoAttribute(),
                        "key_purpose" to keyPurpose.name.asDynamoAttribute(),
                        "key_type" to keyType.name.asDynamoAttribute(),
                        "enabled" to true.asDynamoAttribute(),
                        "key_expiration_date_timestamp" to Duration.ofSeconds(0).toSeconds().asDynamoAttribute()

                    )
                ).build()
        )
    }

    override fun signatureKeys(): Keys {
        val keysOnDynamo = findAllFrom(signatureTableName)
        val items = keysOnDynamo.items()
        val keys = keysListFrom(items)
        return Keys(keys)
    }

    override fun findOne(kid: Kid, keyPurpose: KeyPurpose): Key {
        val tableName = tableNameBasedOn(keyPurpose)
        return dynamoDbClient.getItem(
            GetItemRequest.builder().tableName(tableName).key(
                mapOf(
                    "key_id" to kid.content().asDynamoAttribute()
                )
            ).build()
        ).item().let { keyFromDynamoFor(it) }
    }

    override fun justDeleteKey(kid: Kid, keyPurpose: KeyPurpose) {
        val tableName = tableNameBasedOn(keyPurpose)
        dynamoDbClient.deleteItem(
            DeleteItemRequest.builder().tableName(tableName).key(
                mapOf(
                    "key_id" to kid.content().asDynamoAttribute(),
                )
            ).build()
        )
    }

    override fun keyDeleteJodPlannedFor(kid: Kid, ttl: Duration, keyPurpose: KeyPurpose) {
        try {
            val tableName = tableNameBasedOn(keyPurpose)
            dynamoDbClient.updateItem(
                UpdateItemRequest.builder().tableName(tableName).key(
                    mapOf(
                        "key_id" to kid.content().asDynamoAttribute(),
                    )
                ).updateExpression("set enabled=:enabled, key_expiration_date_timestamp=:timestamp")
                    .expressionAttributeValues(
                        mapOf(
                            ":enabled" to false.asDynamoAttribute(),
                            ":timestamp" to ttl.expirationTimeStampInSecondFromNow(clock).asDynamoAttribute(),
                            ":zero" to Duration.ZERO.toSeconds().asDynamoAttribute()
                        )
                    )
                    .conditionExpression("key_expiration_date_timestamp = :zero")
                    .build()
            )
        } catch (e: ConditionalCheckFailedException) {
            logger.info("The key ${kid.content()} delete request is ignored... key is already disabled")
        }
    }

    private fun tableNameBasedOn(keyPurpose: KeyPurpose) = when (keyPurpose) {
        KeyPurpose.MFA -> mfaTableName
        KeyPurpose.SIGNATURE -> signatureTableName
    }

    private fun keyFromDynamoFor(it: MutableMap<String, AttributeValue>) = Key(
        DataKey.from(
            it.valueAsStringFor("encrypted_private_key"), it.valueAsStringFor("public_key")
        ),
        MasterKid(it.valueAsStringFor("master_key_id")),
        Kid(it.valueAsStringFor("key_id")),
        it.valueAsBoolFor("enabled"),
        KeyType.valueOf(it.valueAsStringFor("key_type")),
        KeyPurpose.valueOf(it.valueAsStringFor("key_purpose")),
        it.valueAsLongFor("key_expiration_date_timestamp", 0)
    )

    private fun findAllFrom(table: String) = dynamoDbClient.scan(
        ScanRequest.builder().tableName(table).build()
    )

    private fun keysListFrom(items: MutableList<MutableMap<String, AttributeValue>>) =
        items.map { keyFromDynamoFor(it) }


}
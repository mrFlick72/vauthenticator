package com.vauthenticator.server.mfa.adapter.dynamodb

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.mfa.domain.*
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest

class DynamoMfaAccountMethodsRepository(
    private val mfaAccountMethodTableName: String,
    private val defaultMfaAccountMethodTableName: String,
    private val dynamoDbClient: DynamoDbClient,
    private val keyRepository: KeyRepository,
    private val masterKid: MasterKid,
    private val mfaDeviceIdGenerator: () -> MfaDeviceId
) : MfaAccountMethodsRepository {

    override fun findBy(
        userName: String,
        mfaMfaMethod: MfaMethod,
        mfaChannel: String
    ): MfaAccountMethod? =
        getFromDynamoBy(userName, mfaChannel)
            .map { MfaAccountMethodMapper.fromDynamoToDomain(userName, it) }
            .find { it.mfaMethod == mfaMfaMethod }

    override fun findBy(mfaDeviceId: MfaDeviceId): MfaAccountMethod? =
        getFromDynamoBy(mfaDeviceId)
            .map { MfaAccountMethodMapper.fromDynamoToDomain(it) }
            .firstOrNull()


    override fun findAll(userName: String): List<MfaAccountMethod> =
        getFromDynamoBy(userName)
            .map { MfaAccountMethodMapper.fromDynamoToDomain(userName, it) }

    private fun getFromDynamoBy(mfaDeviceId: MfaDeviceId) =
        dynamoDbClient.query(
            QueryRequest.builder()
                .tableName(mfaAccountMethodTableName)
                .indexName("${mfaAccountMethodTableName}_Index")
                .keyConditionExpression("mfa_device_id=:mfaDeviceId")
                .expressionAttributeValues(
                    mapOf(
                        ":mfaDeviceId" to mfaDeviceId.content.asDynamoAttribute(),
                    )
                )
                .build()
        ).items()


    private fun getFromDynamoBy(email: String) = dynamoDbClient.query(
        QueryRequest.builder().tableName(mfaAccountMethodTableName).keyConditionExpression("user_name=:email")
            .expressionAttributeValues(mapOf(":email" to email.asDynamoAttribute())).build()
    ).items()

    private fun getFromDynamoBy(email: String, mfaChannel: String) = dynamoDbClient.query(
        QueryRequest.builder().tableName(mfaAccountMethodTableName)
            .keyConditionExpression("user_name=:email AND mfa_channel=:mfaChannel")
            .expressionAttributeValues(
                mapOf(
                    ":email" to email.asDynamoAttribute(),
                    ":mfaChannel" to mfaChannel.asDynamoAttribute(),
                )
            )
            .build()
    ).items()

    override fun save(
        userName: String,
        mfaMfaMethod: MfaMethod,
        mfaChannel: String,
        associated: Boolean
    ): MfaAccountMethod {
        val existingMethod = findBy(userName, mfaMfaMethod, mfaChannel)
        val kid = existingMethod?.key ?: keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA)
        val mfaDeviceId = existingMethod?.mfaDeviceId ?: mfaDeviceIdGenerator.invoke()

        storeOnDynamo(userName, mfaMfaMethod, mfaChannel, mfaDeviceId, kid, associated)
        return MfaAccountMethod(userName, mfaDeviceId, kid, mfaMfaMethod, mfaChannel, associated)
    }

    override fun setAsDefault(userName: String, mfaDeviceId: MfaDeviceId) {
        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(defaultMfaAccountMethodTableName)
                .item(
                    mapOf(
                        "user_name" to userName.asDynamoAttribute(),
                        "mfa_device_id" to mfaDeviceId.content.asDynamoAttribute()
                    )
                ).build()
        )
    }

    override fun getDefaultDevice(userName: String): MfaDeviceId? {
        val queryRequest = QueryRequest.builder()
            .tableName(defaultMfaAccountMethodTableName)
            .keyConditionExpression("user_name=:email")
            .expressionAttributeValues(mapOf(":email" to userName.asDynamoAttribute()))
            .build()

        val mfaDeviceId = dynamoDbClient.query(queryRequest)
            .items()
            .map { MfaDeviceId(it.valueAsStringFor("mfa_device_id")) }
            .firstOrNull()

        return mfaDeviceId
    }

    private fun storeOnDynamo(
        userName: String,
        mfaMfaMethod: MfaMethod,
        mfaChannel: String,
        mfaDeviceId: MfaDeviceId,
        kid: Kid,
        associated: Boolean
    ) {
        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(mfaAccountMethodTableName)
                .item(
                    mapOf(
                        "user_name" to userName.asDynamoAttribute(),
                        "mfa_method" to mfaMfaMethod.name.asDynamoAttribute(),
                        "mfa_channel" to mfaChannel.asDynamoAttribute(),
                        "mfa_device_id" to mfaDeviceId.content.asDynamoAttribute(),
                        "key_id" to kid.content().asDynamoAttribute(),
                        "associated" to associated.asDynamoAttribute()
                    )
                ).build()
        )
    }

    // Not atomic: two concurrent revocations of the same account can both pass the check (docs/mfa.md, ADR 0009).
    override fun revoke(userName: String, mfaDeviceId: MfaDeviceId, check: MfaDeviceRevocationCheck): MfaAccountMethod {
        val accountDevices = findAll(userName)
        val device = accountDevices.find { it.mfaDeviceId == mfaDeviceId }
            ?: throw MfaDeviceNotFoundException("mfa device not found")

        check(MfaDeviceRevocationCandidate(device, accountDevices, getDefaultDevice(userName)))

        dynamoDbClient.deleteItem(
            DeleteItemRequest.builder()
                .tableName(mfaAccountMethodTableName)
                .key(
                    mapOf(
                        "user_name" to userName.asDynamoAttribute(),
                        "mfa_channel" to device.mfaChannel.asDynamoAttribute()
                    )
                ).build()
        )
        return device
    }
}

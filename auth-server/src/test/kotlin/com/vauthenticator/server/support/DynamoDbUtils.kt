package com.vauthenticator.server.support

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URI

object DynamoDbUtils {
    const val dynamoPasswordHistoryTableName: String = "TESTING_VAuthenticator_PasswordHistory"
    const val dynamoClientApplicationTableName: String = "TESTING_VAuthenticator_ClientApplication"
    const val dynamoAccountTableName: String = "TESTING_VAuthenticator_Account"
    const val dynamoRoleTableName: String = "TESTING_VAuthenticator_Role"
    const val dynamoGroupTableName: String = "TESTING_VAuthenticator_Group"
    const val dynamoGroupToRoleAssociationTableName: String = "TESTING_VAuthenticator_Group_To_Role_Association"
    const val dynamoSignatureKeysTableName: String = "TESTING_VAuthenticator_Signature_Keys"
    const val dynamoMfaKeysTableName: String = "TESTING_VAuthenticator_Mfa_Keys"
    const val dynamoTicketTableName: String = "TESTING_VAuthenticator_ticket"
    const val dynamoMfaAccountMethodsTableName: String = "TESTING_VAuthenticator_Mfa_Account_Methods"
    const val dynamoDefaultMfaAccountMethodsTableName: String = "TESTING_VAuthenticator_Default_Mfa_Account_Methods"

    val dynamoDbClient: DynamoDbClient = DynamoDbClient.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create("ACCESS_KEY_ID", "SECRET_ACCESS_KEY")
            )
        ).region(Region.US_EAST_1)
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()

    fun initRoleTestsInDynamo() {
        val roleName = AttributeValue.builder().s("a_role").build()
        val description = AttributeValue.builder().s("A_ROLE").build()
        val item = PutItemRequest.builder()
            .tableName(dynamoRoleTableName)
            .item(
                mutableMapOf(
                    "role_name" to roleName,
                    "description" to description
                )
            )
            .build()
        dynamoDbClient.putItem(item)
    }

    fun resetDynamoDb() {
        try {
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoPasswordHistoryTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoClientApplicationTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoAccountTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoRoleTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoGroupTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoGroupToRoleAssociationTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoSignatureKeysTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoMfaKeysTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoTicketTableName)
                    .build()
            )
            dynamoDbClient.deleteTable(
                DeleteTableRequest.builder()
                    .tableName(dynamoMfaAccountMethodsTableName)
                    .build()
            )
        } catch (e: java.lang.Exception) {
        }
        try {
            createDynamoPasswordHistoryTable()
            createDynamoClientApplicationTable()
            createDynamoAccountTable()
            createDynamoRoleTable()
            createDynamoGroupTable()
            createDynamoGroupToRoleAssociationTable()
            createDynamoSignatureKeysTable()
            createDynamoMfaKeysTable()
            createDynamoTicketTable()
            createDynamoMfaAccountMethodsTable()
            createDynamoDefaultMfaAccountMethodsTable()
        } catch (e: java.lang.Exception) {
        }
    }

    private fun createDynamoPasswordHistoryTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoPasswordHistoryTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("user_name")
                        .keyType(KeyType.HASH)
                        .build(),
                    KeySchemaElement.builder()
                        .attributeName("created_at")
                        .keyType(KeyType.RANGE)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("user_name")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("created_at")
                        .attributeType(ScalarAttributeType.N)

                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoMfaAccountMethodsTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoMfaAccountMethodsTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("user_name")
                        .keyType(KeyType.HASH)
                        .build(),
                    KeySchemaElement.builder()
                        .attributeName("mfa_channel")
                        .keyType(KeyType.RANGE)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("user_name")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("mfa_channel")
                        .attributeType(ScalarAttributeType.S)
                        .build(), AttributeDefinition.builder()
                        .attributeName("mfa_device_id")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .indexName("${dynamoMfaAccountMethodsTableName}_Index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("mfa_device_id")
                                .keyType(KeyType.HASH)
                                .build(),
                        )
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoDefaultMfaAccountMethodsTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoDefaultMfaAccountMethodsTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("user_name")
                        .keyType(KeyType.HASH)
                        .build(),
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("user_name")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoRoleTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoRoleTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("role_name")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("role_name")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoGroupTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoGroupTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("group_name")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("group_name")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoGroupToRoleAssociationTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoGroupToRoleAssociationTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("group_name")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("group_name")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoAccountTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoAccountTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("user_name")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("user_name")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoClientApplicationTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoClientApplicationTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("client_id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("client_id")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoSignatureKeysTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoSignatureKeysTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("key_id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("key_id")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        )
    }

    private fun createDynamoMfaKeysTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoMfaKeysTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("key_id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("key_id")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        );
    }

    private fun createDynamoTicketTable() {
        dynamoDbClient.createTable(
            CreateTableRequest.builder()
                .tableName(dynamoTicketTableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("ticket")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("ticket")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build()
        );
    }

}
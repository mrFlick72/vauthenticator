package com.vauthenticator.server.mfa.adapter.dynamodb

import com.vauthenticator.server.mfa.adapter.AbstractMfaAccountMethodsRepositoryTest
import com.vauthenticator.server.mfa.domain.MfaAccountMethodsRepository
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDefaultMfaAccountMethodsTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoMfaAccountMethodsTableName
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DynamoMfaAccountMethodsRepositoryTest : AbstractMfaAccountMethodsRepositoryTest() {

    override fun initMfaAccountMethodsRepository() =
        DynamoMfaAccountMethodsRepository(
            dynamoMfaAccountMethodsTableName,
            dynamoDefaultMfaAccountMethodsTableName,
            dynamoDbClient,
            keyRepository,
            masterKid
        ) { mfaDeviceId }

    override fun resetDatabase() {
        resetDynamoDb()
    }
}
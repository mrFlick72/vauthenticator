package com.vauthenticator.server.oauth2.clientapp.adapter.dynamodb

import com.vauthenticator.server.oauth2.clientapp.adapter.AbstractClientApplicationRepositoryTest
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.support.DynamoDbUtils.dynamoClientApplicationTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb

class DynamoDbClientApplicationRepositoryTest : AbstractClientApplicationRepositoryTest() {

    override fun resetDatabase() {
        resetDynamoDb()
    }

    override fun initUnitUnderTest(allowedOriginRepository: AllowedOriginRepository): ClientApplicationRepository =
        DynamoDbClientApplicationRepository(dynamoDbClient, dynamoClientApplicationTableName, allowedOriginRepository)


}
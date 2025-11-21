package com.vauthenticator.server.role.adapter.dynamodb

import com.vauthenticator.server.role.adapter.AbstractRoleRepositoryTest
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoRoleTableName
import com.vauthenticator.server.support.DynamoDbUtils.initRoleTestsInDynamo
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import com.vauthenticator.server.support.protectedRoleNames

class DynamoDbRoleRepositoryTest : AbstractRoleRepositoryTest() {
    override fun initRoleRepository(): RoleRepository =
        DynamoDbRoleRepository(protectedRoleNames, dynamoDbClient, dynamoRoleTableName)

    override fun resetDatabase() {
        resetDynamoDb()
        initRoleTestsInDynamo()
    }

}
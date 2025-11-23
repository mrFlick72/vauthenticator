package com.vauthenticator.server.account.adapter.dynamodb

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.adapter.AbstractAccountRepositoryTest
import com.vauthenticator.server.role.adapter.dynamodb.DynamoDbRoleRepository
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.DynamoDbUtils.dynamoAccountTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoRoleTableName
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import com.vauthenticator.server.support.protectedRoleNames


internal class DynamoDbAbstractAccountRepositoryTest : AbstractAccountRepositoryTest() {


    override fun initUnitUnderTest(roleRepository: RoleRepository): AccountRepository =
        DynamoDbAccountRepository(
            dynamoDbClient,
            dynamoAccountTableName,
            roleRepository
        )

    override fun initRoleRepository(): RoleRepository =
        DynamoDbRoleRepository(
            protectedRoleNames,
            dynamoDbClient,
            dynamoRoleTableName
        )


    override fun resetDatabase() {
        resetDynamoDb()
    }

}
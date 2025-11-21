package com.vauthenticator.server.role.adapter.dynamodb

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vauthenticator.server.role.adapter.AbstractGroupRepositoryTest
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoGroupTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoGroupToRoleAssociationTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoRoleTableName
import com.vauthenticator.server.support.DynamoDbUtils.initRoleTestsInDynamo
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import com.vauthenticator.server.support.protectedRoleNames

class DynamoDbGroupRepositoryTest : AbstractGroupRepositoryTest() {
    override fun initGroupRepository(): GroupRepository =
        DynamoDbGroupRepository(jacksonObjectMapper(), dynamoGroupTableName, dynamoGroupToRoleAssociationTableName, dynamoDbClient, initRoleRepository())

    override fun initRoleRepository(): RoleRepository =
        DynamoDbRoleRepository(protectedRoleNames, dynamoDbClient, dynamoRoleTableName)

    override fun resetDatabase() {
        resetDynamoDb()
        initRoleTestsInDynamo()

        roleRepository.save(Role("a_role_name", "a_role_description"))
        roleRepository.save(Role("another_role_name", "another_role_description"))
    }

}
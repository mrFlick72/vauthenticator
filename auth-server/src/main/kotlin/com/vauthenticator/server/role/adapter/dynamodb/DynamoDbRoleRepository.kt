package com.vauthenticator.server.role.adapter.dynamodb

import com.vauthenticator.server.role.domain.ProtectedRoleFromDeletionException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class DynamoDbRoleRepository(
    private val protectedRoleFromDeletion: List<String>,
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : RoleRepository {

    override fun findAll() =
        dynamoDbClient.scan(DynamoDbRoleMapper.findAllRequestFor(tableName))
            .items()
            .map(DynamoDbRoleMapper::roleFor)

    override fun save(role: Role) =
        dynamoDbClient.putItem(DynamoDbRoleMapper.saveRoleRequestFor(role, tableName))
            .let { }

    override fun delete(roleName: String) =
        if (protectedRoleFromDeletion.contains(roleName)) {
            throw ProtectedRoleFromDeletionException("the role $roleName is not allowed to be deleted")
        } else {
            dynamoDbClient.deleteItem(DynamoDbRoleMapper.deleteRoleRequestFor(roleName, tableName))
                .let { }
        }


}
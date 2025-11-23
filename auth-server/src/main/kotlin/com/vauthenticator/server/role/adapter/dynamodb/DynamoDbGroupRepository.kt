package com.vauthenticator.server.role.adapter.dynamodb

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.filterEmptyMetadata
import com.vauthenticator.server.extentions.valueAsLongFor
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.role.domain.*
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.util.*
import kotlin.jvm.optionals.getOrNull

private const val GROUP_NAME_FIELD_NAME = "group_name"
private const val DESCRIPTION_FIELD_NAME = "description"
private const val ROLES_FIELD_NAME = "roles"
private const val VERSION_FIELD_NAME = "version"

class DynamoDbGroupRepository(
    private val objectMapper: ObjectMapper,
    private val groupTableName: String,
    private val groupToRoleAssociationTableName: String,
    private val dynamoDbClient: DynamoDbClient,
    private val roleRepository: RoleRepository
) : GroupRepository {
    override fun loadFor(groupName: String): GroupWitRoles? {
        return Optional.ofNullable(
            dynamoDbClient.getItem {
                it.tableName(groupTableName)
                    .key(
                        mutableMapOf(
                            GROUP_NAME_FIELD_NAME to groupName.asDynamoAttribute()
                        )
                    )
            }.item()
        ).flatMap { it.filterEmptyMetadata() }
            .map {
                val roles = dynamoDbClient.getItem {
                    it.tableName(groupToRoleAssociationTableName)
                        .key(
                            mutableMapOf(
                                GROUP_NAME_FIELD_NAME to groupName.asDynamoAttribute()
                            )
                        )
                }.item()
                    .let {
                        rolesFrom(it)
                    }

                GroupWitRoles(
                    group = Group(
                        name = it.valueAsStringFor(GROUP_NAME_FIELD_NAME),
                        description = it.valueAsStringFor(DESCRIPTION_FIELD_NAME)
                    ),
                    roles = roles
                )
            }
            .map { stealRoleCleanUpFor(it) }
            .getOrNull()

    }

    override fun findAll(): List<Group> {
        return dynamoDbClient.scan {
            it.tableName(groupTableName)
        }.items()
            .map {
                Group(
                    it.valueAsStringFor(GROUP_NAME_FIELD_NAME),
                    it.valueAsStringFor(DESCRIPTION_FIELD_NAME)
                )
            }
    }

    override fun save(group: Group) {
        dynamoDbClient.putItem {
            it.item(
                mutableMapOf(
                    GROUP_NAME_FIELD_NAME to group.name.asDynamoAttribute(),
                    DESCRIPTION_FIELD_NAME to group.description.asDynamoAttribute()
                )
            ).tableName(groupTableName)
        }
    }

    override fun delete(groupName: String) {
        dynamoDbClient.deleteItem {
            it.tableName(groupToRoleAssociationTableName)
                .key(
                    mutableMapOf(
                        GROUP_NAME_FIELD_NAME to groupName.asDynamoAttribute()
                    )
                )
        }

        dynamoDbClient.deleteItem {
            it.tableName(groupTableName)
                .key(
                    mutableMapOf(
                        GROUP_NAME_FIELD_NAME to groupName.asDynamoAttribute()
                    )
                )
        }
    }

    override fun roleAssociation(groupName: String, vararg roleNames: String) {
        roleAssociation(groupName, arrayListOf(*roleNames), true)
    }

    override fun roleDeAssociation(groupName: String, vararg roleNames: String) {
        roleAssociation(groupName, arrayListOf(*roleNames), false)
    }


    private fun roleAssociation(
        groupName: String,
        roleNames: List<String>,
        associate: Boolean
    ) {
        var version = 1L
        var emptyRolesAssociation = true
        val allRoles = roleRepository.findAll()
        val roles = dynamoDbClient.getItem {
            it.tableName(groupToRoleAssociationTableName)
                .key(
                    mutableMapOf(
                        GROUP_NAME_FIELD_NAME to groupName.asDynamoAttribute()
                    )
                )
        }
            .item()
            .let {
                version = it.valueAsLongFor(VERSION_FIELD_NAME, 1)
                val rolesFromDb = rolesFrom(it)
                if (rolesFromDb.isNotEmpty()) {
                    emptyRolesAssociation = false;
                }
                val associatedRoles = roleNames.map {
                    Role(
                        it,
                        allRoles.find { role -> role.name == it }?.description ?: ""
                    )
                }.toSet()

                if (associate) {
                    rolesFromDb + associatedRoles
                } else {
                    rolesFromDb - associatedRoles
                }.toSet()

            }

        val updateItemRequestBuilder = PutItemRequest.builder()
            .tableName(groupToRoleAssociationTableName)
            .item(
                mutableMapOf(
                    GROUP_NAME_FIELD_NAME to groupName.asDynamoAttribute(),
                    ROLES_FIELD_NAME to roles.map { objectMapper.writeValueAsString(it) }.asDynamoAttribute(),
                    VERSION_FIELD_NAME to (version + 1).asDynamoAttribute()
                )
            )
        if (!emptyRolesAssociation) {
            updateItemRequestBuilder.conditionExpression("$VERSION_FIELD_NAME = :version")
                .expressionAttributeValues(mutableMapOf(":version" to (version).asDynamoAttribute()))
        }

        dynamoDbClient.putItem(updateItemRequestBuilder.build())
    }

    private fun rolesFrom(map: MutableMap<String, AttributeValue>): List<Role> {
        return if (map.isNotEmpty()) {
            map.getValue(ROLES_FIELD_NAME).ss().map { objectMapper.readValue(it, Role::class.java) }

        } else {
            emptyList()
        }
    }

    private fun stealRoleCleanUpFor(group: GroupWitRoles): GroupWitRoles {
        val roles = roleRepository.findAll()
        val filteredRoles = group.roles.filter { role -> roles.map { it.name }.contains(role.name) }.toSet().toList()
        val updatedGroup = group.copy(roles = filteredRoles)
        if (filteredRoles != group.roles) {
            roleAssociation(group.group.name, filteredRoles.map { it.name }, false)
        }
        return updatedGroup
    }

}
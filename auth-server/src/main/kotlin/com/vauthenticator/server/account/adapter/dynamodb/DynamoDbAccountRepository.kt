package com.vauthenticator.server.account.adapter.dynamodb

import com.vauthenticator.server.account.adapter.dynamodb.DynamoAccountConverter.fromDynamoToDomain
import com.vauthenticator.server.account.adapter.dynamodb.DynamoAccountQueryFactory.findAccountQueryForUserName
import com.vauthenticator.server.account.adapter.dynamodb.DynamoAccountQueryFactory.storeAccountQueryFor
import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRegistrationException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.extentions.filterEmptyMetadata
import com.vauthenticator.server.role.domain.RoleRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import java.util.*
import java.util.Optional.ofNullable

class DynamoDbAccountRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val dynamoAccountTableName: String,
    private val roleRepository: RoleRepository
) : AccountRepository {

    override fun accountFor(username: String): Optional<Account> =
        ofNullable(findAccountFor(username))
            .flatMap { it.filterEmptyMetadata() }
            .map(::fromDynamoToDomain)
            .map(::stealRoleCleanUpFor)


    private fun stealRoleCleanUpFor(account: Account): Account {
        val roles = roleRepository.findAll()
        val filteredAuthorities = account.authorities.filter { role -> roles.map { it.name }.contains(role) }.toSet()
        val updatedAccount = account.copy(authorities = filteredAuthorities)
        if (filteredAuthorities != account.authorities) {
            save(updatedAccount)
        }
        return updatedAccount
    }

    private fun findAccountFor(username: String) =
        dynamoDbClient.getItem(
            findAccountQueryForUserName(username, dynamoAccountTableName)
        ).item()

    override fun save(account: Account) {
        storeAccountFrom(account = account, withUpsert = true)
    }

    private fun storeAccountFrom(account: Account, withUpsert: Boolean) {
        dynamoDbClient.putItem(storeAccountQueryFor(account, dynamoAccountTableName, withUpsert))
    }

    override fun create(account: Account) = try {
        storeAccountFrom(account = account, withUpsert = false)
    } catch (e: ConditionalCheckFailedException) {
        throw AccountRegistrationException("account already created", e)
    }

}
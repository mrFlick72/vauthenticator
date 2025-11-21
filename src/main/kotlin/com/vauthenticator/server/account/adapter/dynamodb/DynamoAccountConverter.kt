package com.vauthenticator.server.account.adapter.dynamodb

import com.vauthenticator.server.account.domain.*
import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.valueAsBoolFor
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.extentions.valueAsStringSetFor
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object DynamoAccountConverter {
    fun fromDynamoToDomain(
        dynamoPayload: MutableMap<String, AttributeValue>,
    ) = Account(
        accountNonExpired = dynamoPayload.valueAsBoolFor("accountNonExpired"),
        accountNonLocked = dynamoPayload.valueAsBoolFor("accountNonLocked"),
        credentialsNonExpired = dynamoPayload.valueAsBoolFor("credentialsNonExpired"),
        enabled = dynamoPayload.valueAsBoolFor("enabled"),
        username = dynamoPayload.valueAsStringFor("user_name"),
        password = dynamoPayload.valueAsStringFor("password"),
        email = dynamoPayload.valueAsStringFor("email"),
        emailVerified = dynamoPayload.valueAsBoolFor("emailVerified"),
        firstName = dynamoPayload.valueAsStringFor("firstName"),
        lastName = dynamoPayload.valueAsStringFor("lastName"),
        authorities = dynamoPayload.valueAsStringSetFor("authorities"),
        birthDate = Date.isoDateFor(
            dynamoPayload.valueAsStringFor(
                "birthDate"
            )
        ),
        phone = Phone.phoneFor(
            dynamoPayload.valueAsStringFor(
                "phone"
            )
        ),
        locale = UserLocale.localeFrom(
            dynamoPayload.valueAsStringFor(
                "locale"
            )
        ),
        mandatoryAction = AccountMandatoryAction.valueOf(
            dynamoPayload.valueAsStringFor("mandatory_action")
        ),
        groups = dynamoPayload.valueAsStringSetFor("group")
    )

    fun fromDomainToDynamo(account: Account) =
        mutableMapOf(
            "accountNonExpired" to account.accountNonExpired.asDynamoAttribute(),
            "accountNonLocked" to account.accountNonLocked.asDynamoAttribute(),
            "credentialsNonExpired" to account.credentialsNonExpired.asDynamoAttribute(),
            "enabled" to account.enabled.asDynamoAttribute(),
            "user_name" to account.username.asDynamoAttribute(),
            "password" to account.password.asDynamoAttribute(),
            "email" to account.email.asDynamoAttribute(),
            "emailVerified" to account.emailVerified.asDynamoAttribute(),
            "authorities" to account.authorities.asDynamoAttribute(),
            "firstName" to account.firstName.asDynamoAttribute(),
            "lastName" to account.lastName.asDynamoAttribute(),
            "birthDate" to account.birthDate.map { it.asDynamoAttribute() }.orElse("".asDynamoAttribute()),
            "phone" to account.phone.map { it.asDynamoAttribute() }.orElse("".asDynamoAttribute()),
            "locale" to account.locale.map { it.formattedLocale().asDynamoAttribute() }.orElse("".asDynamoAttribute()),
            "mandatory_action" to account.mandatoryAction.let { it.name }.asDynamoAttribute()
        )
}
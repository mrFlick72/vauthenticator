package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.*
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun Scopes.asDynamoAttribute(): AttributeValue = AttributeValue.builder().ss(this.content.map { it.content }).build()

fun AllowedOrigins.asDynamoAttribute(): AttributeValue =
    AttributeValue.builder().ss(this.content.map { it.content }).build()

fun AuthorizedGrantTypes.asDynamoAttribute(): AttributeValue =
    AttributeValue.builder().ss(this.content.map { it.name }).build()

fun Authorities.asDynamoAttribute(): AttributeValue =
    AttributeValue.builder().ss(this.content.map { it.content }).build()

fun TokenTimeToLive.asDynamoAttribute(): AttributeValue =
    AttributeValue.builder().n(this.content.toString()).build()

fun ClientApplication.hasEnoughScopes(scopes: Scopes): Boolean {
    return scopes.content.stream().allMatch { this.scopes.content.contains(it) }
        .or(this.scopes.content.contains(Scope.ADMIN_FULL_ACCESS))

}

fun ClientApplication.hasEnoughScopes(scope: Scope) = hasEnoughScopes(Scopes(setOf(scope)))
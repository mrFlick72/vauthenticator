package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun Map<String, String>.asDynamoAttribute(): AttributeValue =
    this.map { mutableMapOf(it.key to AttributeValue.builder().s(it.value).build()) }
        .reduce { a, b -> a.plus(b) as MutableMap<String, AttributeValue> }
        .let { AttributeValue.fromM(it) }

fun Map<String, AttributeValue>.valueAsMapFor(key: String): Map<String, String> =
    this[key]
        ?.m()
        ?.entries
        ?.associate { it.key to it.value.s() }
        ?: emptyMap()

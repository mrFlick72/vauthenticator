package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.util.*

fun Map<String, String>.asDynamoAttribute(): AttributeValue =
    this.map { mutableMapOf(it.key to AttributeValue.builder().s(it.value).build()) }
        .reduce { a, b -> a.plus(b) as MutableMap<String, AttributeValue> }
        .let { AttributeValue.fromM(it) }

fun Map<String, AttributeValue>.valueAsMapFor(key: String): Map<String, String> =
    Optional.ofNullable(this[key])
        .map { it.m() }
        .map { it.entries }
        .map { it.map { mutableMapOf(it.key to it.value.s()) } }
        .map { it.reduce { acc, mutableMap -> (acc + mutableMap) as MutableMap<String, String> } }
        .orElseGet { mutableMapOf<String, String>() }

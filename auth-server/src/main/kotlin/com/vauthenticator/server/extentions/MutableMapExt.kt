package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun MutableMap<String, AttributeValue>.valueAsStringFor(key: String): String =
    this[key]?.s()!!

fun MutableMap<String, AttributeValue>.valueAsStringFor(key: String, default: String): String =
    this[key]?.s() ?: default

fun MutableMap<String, AttributeValue>.valuesAsListOfStringFor(key: String): List<String> =
    this[key]?.ss() ?: emptyList()

fun MutableMap<String, AttributeValue>.valueAsBoolFor(key: String): Boolean =
    this[key]?.bool()!!

fun MutableMap<String, AttributeValue>.valueAsStringSetFor(key: String): Set<String> =
    this[key]?.ss()?.toSet() ?: emptySet()

fun MutableMap<String, AttributeValue>.valueAsLongFor(key: String): Long =
    this[key]?.n()!!.toLong()

fun MutableMap<String, AttributeValue>.valueAsLongFor(key: String, default: Long): Long =
    this[key]?.n()?.toLong() ?: default

fun MutableMap<String, AttributeValue>.filterEmptyMetadata(): MutableMap<String, AttributeValue>? =
    takeIf { it.isNotEmpty() }

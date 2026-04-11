package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class DynamoDbAttributeException(message: String) : RuntimeException(message)

fun Map<String, AttributeValue>.valueAsStringFor(key: String): String =
    requiredValueFor(key) { s() }

fun Map<String, AttributeValue>.valueAsStringFor(key: String, default: String): String =
    this[key]?.s() ?: default

fun Map<String, AttributeValue>.valuesAsListOfStringFor(key: String): List<String> =
    this[key]?.ss() ?: emptyList()

fun Map<String, AttributeValue>.valueAsBoolFor(key: String): Boolean =
    requiredValueFor(key) { bool() }

fun Map<String, AttributeValue>.valueAsStringSetFor(key: String): Set<String> =
    this[key]?.ss()?.toSet() ?: emptySet()

fun Map<String, AttributeValue>.valueAsLongFor(key: String): Long =
    requiredValueFor(key) { n() }
        .toLongOrNull()
        ?: throw DynamoDbAttributeException("DynamoDB attribute $key is not a valid Long")

fun Map<String, AttributeValue>.valueAsLongFor(key: String, default: Long): Long =
    this[key]?.n()?.toLong() ?: default

fun MutableMap<String, AttributeValue>.filterEmptyMetadata(): MutableMap<String, AttributeValue>? =
    takeIf { it.isNotEmpty() }

private inline fun <T : Any> Map<String, AttributeValue>.requiredValueFor(
    key: String,
    extractor: AttributeValue.() -> T?
): T =
    this[key]?.extractor()
        ?: throw DynamoDbAttributeException("Missing or invalid DynamoDB attribute $key")

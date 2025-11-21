package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.util.*

fun MutableMap<String, AttributeValue>.valueAsStringFor(key: String): String =
    this[key]?.s()!!

fun MutableMap<String, AttributeValue>.valueAsStringFor(key: String, default: String): String =
    Optional.ofNullable(this[key]).map { this[key]?.s()!! }.orElse(default)

fun MutableMap<String, AttributeValue>.valuesAsListOfStringFor(key: String): List<String> =
    Optional.ofNullable(this[key]).map { it.ss() }.orElse(emptyList())

fun MutableMap<String, AttributeValue>.valueAsBoolFor(key: String): Boolean =
    this[key]?.bool()!!

fun MutableMap<String, AttributeValue>.valueAsStringSetFor(key: String): Set<String> =
    this[key]?.ss()?.toSet() ?: emptySet()

fun MutableMap<String, AttributeValue>.valueAsLongFor(key: String): Long =
    this[key]?.n()!!.toLong()

fun MutableMap<String, AttributeValue>.valueAsLongFor(key: String, default: Long): Long =
    Optional.ofNullable(this[key]).map { it.n()!!.toLong() }.orElse(default)

fun MutableMap<String, AttributeValue>.filterEmptyMetadata() =
    if (this.isEmpty()) {
        Optional.empty()
    } else {
        Optional.of(this)
    }
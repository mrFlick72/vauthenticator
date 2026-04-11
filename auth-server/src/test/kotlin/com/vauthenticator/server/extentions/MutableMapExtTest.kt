package com.vauthenticator.server.extentions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class MutableMapExtTest {

    @Test
    fun `string value is read from attribute map`() {
        val attributes = mapOf(
            "name" to AttributeValue.builder().s("value").build()
        )

        assertEquals("value", attributes.valueAsStringFor("name"))
    }

    @Test
    fun `bool value is read from attribute map`() {
        val attributes = mapOf(
            "enabled" to AttributeValue.builder().bool(true).build()
        )

        assertEquals(true, attributes.valueAsBoolFor("enabled"))
    }

    @Test
    fun `long value is read from attribute map`() {
        val attributes = mapOf(
            "ttl" to AttributeValue.builder().n("42").build()
        )

        assertEquals(42L, attributes.valueAsLongFor("ttl"))
    }

    @Test
    fun `missing required attribute throws a descriptive exception`() {
        val attributes = emptyMap<String, AttributeValue>()

        val actual = assertThrows(DynamoDbAttributeException::class.java) {
            attributes.valueAsStringFor("name")
        }

        assertEquals("Missing or invalid DynamoDB attribute name", actual.message)
    }

    @Test
    fun `invalid attribute type throws a descriptive exception`() {
        val attributes = mapOf(
            "enabled" to AttributeValue.builder().s("true").build()
        )

        val actual = assertThrows(DynamoDbAttributeException::class.java) {
            attributes.valueAsBoolFor("enabled")
        }

        assertEquals("Missing or invalid DynamoDB attribute enabled", actual.message)
    }

    @Test
    fun `invalid long attribute throws a descriptive exception`() {
        val attributes = mapOf(
            "ttl" to AttributeValue.builder().n("not-a-number").build()
        )

        val actual = assertThrows(DynamoDbAttributeException::class.java) {
            attributes.valueAsLongFor("ttl")
        }

        assertEquals("DynamoDB attribute ttl is not a valid Long", actual.message)
    }
}

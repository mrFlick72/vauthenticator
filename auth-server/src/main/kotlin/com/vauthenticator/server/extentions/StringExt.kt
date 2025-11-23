package com.vauthenticator.server.extentions

import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val logger = LoggerFactory.getLogger(StringExt::class.java.name)

class StringExt

fun String.toSha256(): String {
    var messageDigest: MessageDigest? = null
    try {
        messageDigest = MessageDigest.getInstance("SHA-256")
    } catch (e: NoSuchAlgorithmException) {
        logger.error(e.message, e)
    }
    val digest = messageDigest!!.digest(this.toByteArray())
    return java.lang.String.format("%064x", BigInteger(1, digest))
}

fun String.asDynamoAttribute(): AttributeValue = AttributeValue.builder().s(this).build()
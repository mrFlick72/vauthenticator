package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun Boolean.asDynamoAttribute(): AttributeValue = AttributeValue.builder().bool(this).build()

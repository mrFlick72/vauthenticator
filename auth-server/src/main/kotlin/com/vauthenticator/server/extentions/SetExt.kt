package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun Set<String>.asDynamoAttribute(): AttributeValue = AttributeValue.builder().ss(this).build()
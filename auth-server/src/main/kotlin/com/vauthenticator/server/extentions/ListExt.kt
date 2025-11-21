package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun List<String>.asDynamoAttribute(): AttributeValue = AttributeValue.builder().ss(this).build()


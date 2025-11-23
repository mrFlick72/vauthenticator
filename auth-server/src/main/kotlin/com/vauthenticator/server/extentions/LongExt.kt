package com.vauthenticator.server.extentions

import software.amazon.awssdk.services.dynamodb.model.AttributeValue


fun Long.asDynamoAttribute(): AttributeValue =
        AttributeValue.builder().n(this.toString()).build()
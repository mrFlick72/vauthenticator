package com.vauthenticator.server.extentions

import com.vauthenticator.server.account.domain.Phone
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun Phone.asDynamoAttribute() : AttributeValue = AttributeValue.builder().s(this.formattedPhone()).build()
package com.vauthenticator.server.mfa.adapter.dynamodb

import com.vauthenticator.server.extentions.valueAsBoolFor
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.mfa.domain.MfaAccountMethod
import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.mfa.domain.MfaMethod.valueOf
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

object MfaAccountMethodMapper {
    fun fromDynamoToDomain(
        userName: String,
        item: MutableMap<String, AttributeValue>
    ): MfaAccountMethod =
        MfaAccountMethod(
            userName,
            MfaDeviceId(item.valueAsStringFor("mfa_device_id")),
            Kid(item.valueAsStringFor("key_id")),
            valueOf(item.valueAsStringFor("mfa_method")),
            item.valueAsStringFor("mfa_channel"),
            item.valueAsBoolFor("associated")
        )

    fun fromDynamoToDomain(
        item: MutableMap<String, AttributeValue>
    ): MfaAccountMethod =
        MfaAccountMethod(
            item.valueAsStringFor("user_name"),
            MfaDeviceId(item.valueAsStringFor("mfa_device_id")),
            Kid(item.valueAsStringFor("key_id")),
            valueOf(item.valueAsStringFor("mfa_method")),
            item.valueAsStringFor("mfa_channel"),
            item.valueAsBoolFor("associated")
        )
}
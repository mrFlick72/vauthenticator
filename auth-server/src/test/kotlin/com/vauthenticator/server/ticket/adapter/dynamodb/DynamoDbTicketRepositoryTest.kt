package com.vauthenticator.server.ticket.adapter.dynamodb

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.valueAsLongFor
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.DynamoDbUtils
import com.vauthenticator.server.support.DynamoDbUtils.dynamoTicketTableName
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.TicketFixture.ticketFor
import com.vauthenticator.server.ticket.adapter.AbstractTicketRepositoryTest
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.util.*

class DynamoDbTicketRepositoryTest : AbstractTicketRepositoryTest() {

    override fun initTicketRepository(): TicketRepository =
        DynamoDbTicketRepository(DynamoDbUtils.dynamoDbClient, dynamoTicketTableName)

    override fun resetDatabase() {
        resetDynamoDb()
    }

    override fun getActual(): Map<String, Any> {
        val ticketGenerator = getTicketGenerator()
        val item = DynamoDbUtils.dynamoDbClient.getItem(
            GetItemRequest.builder()
                .tableName(dynamoTicketTableName)
                .key(
                    mapOf(
                        "ticket" to ticketGenerator.invoke().asDynamoAttribute()
                    )
                )
                .build()
        ).item()

        return mapOf(
            "ticket" to item["ticket"]!!.s(),
            "ttl" to item["ttl"]!!.n()
        )
    }

}
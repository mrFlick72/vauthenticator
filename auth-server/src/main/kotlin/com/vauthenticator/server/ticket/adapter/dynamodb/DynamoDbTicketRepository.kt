package com.vauthenticator.server.ticket.adapter.dynamodb

import com.vauthenticator.server.extentions.*
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.util.*

class DynamoDbTicketRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : TicketRepository {

    override fun store(ticket: Ticket) {
        dynamoDbClient.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        "ticket" to ticket.ticketId.content.asDynamoAttribute(),
                        "ttl" to (ticket.ttl).asDynamoAttribute(),
                        "user_name" to ticket.userName.asDynamoAttribute(),
                        "client_application_id" to ticket.clientAppId.asDynamoAttribute(),
                        "context" to ticket.context.content.asDynamoAttribute()
                    )
                )
                .build()
        )
    }


    override fun loadFor(ticketId: TicketId): Optional<Ticket> {
        return Optional.ofNullable(
            dynamoDbClient.getItem(
                GetItemRequest.builder()
                    .tableName(tableName)
                    .key(mapOf("ticket" to ticketId.content.asDynamoAttribute()))
                    .build()
            ).item()
        )
            .flatMap { it.filterEmptyMetadata() }
            .map {
                Ticket(
                    TicketId(it.valueAsStringFor("ticket")),
                    it.valueAsStringFor("user_name"),
                    it.valueAsStringFor("client_application_id"),
                    it.valueAsLongFor("ttl"),
                    TicketContext(it.valueAsMapFor("context"))
                )
            }
    }

    override fun delete(ticketId: TicketId) {
        dynamoDbClient.deleteItem(
            DeleteItemRequest.builder()
                .tableName(tableName)
                .key(mapOf("ticket" to ticketId.content.asDynamoAttribute()))
                .build()
        )
    }

}
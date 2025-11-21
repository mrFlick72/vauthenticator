package com.vauthenticator.server.ticket

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.ticket.adapter.dynamodb.DynamoDbTicketRepository
import com.vauthenticator.server.ticket.adapter.jdbc.JdbcTicketRepository
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketFeatures
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Clock
import java.time.Duration
import java.util.*

@Configuration(proxyBeanMethods = false)
class TicketConfig {

    @Bean("ticketRepository")
    @Profile("dynamo")
    fun dynamoDbTicketRepository(
        @Value("\${vauthenticator.dynamo-db.ticket.table-name}") tableName: String,
        dynamoDbClient: DynamoDbClient
    ) = DynamoDbTicketRepository(dynamoDbClient, tableName)


    @Bean("ticketRepository")
    @Profile("database")
    fun jdbCTicketRepository(
        jdbcTemplate: JdbcTemplate,
        objectMapper: ObjectMapper
    ) = JdbcTicketRepository(jdbcTemplate, objectMapper)


    @Bean
    fun verificationTicketFactory(clock: Clock, ticketRepository: TicketRepository) =
        TicketCreator(
            { UUID.randomUUID().toString() },
            clock,
            ticketRepository,
            TicketFeatures(Duration.ofMinutes(5))
        )


}
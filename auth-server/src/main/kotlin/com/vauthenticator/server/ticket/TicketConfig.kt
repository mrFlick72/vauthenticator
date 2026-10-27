package com.vauthenticator.server.ticket

import com.vauthenticator.server.ticket.adapter.jdbc.JdbcTicketRepository
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketFeatures
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Duration
import java.util.*

@Configuration(proxyBeanMethods = false)
class TicketConfig {

    @Bean("ticketRepository")
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
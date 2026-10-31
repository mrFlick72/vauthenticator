package com.vauthenticator.server.ticket

import com.vauthenticator.server.ticket.adapter.jdbc.JdbcTicketRepository
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketFeatures
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.beans.factory.annotation.Value
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
        objectMapper: ObjectMapper,
        clock: Clock
    ) = JdbcTicketRepository(jdbcTemplate, objectMapper, clock)


    @Bean
    fun verificationTicketFactory(
        clock: Clock,
        ticketRepository: TicketRepository,
        @Value("\${ticket.ttl.mfa-enrollment:5m}") mfaEnrollmentTtl: Duration,
        @Value("\${ticket.ttl.reset-password:15m}") resetPasswordTtl: Duration,
        @Value("\${ticket.ttl.email-verification:24h}") emailVerificationTtl: Duration
    ) =
        TicketCreator(
            { UUID.randomUUID().toString() },
            clock,
            ticketRepository,
            TicketFeatures(mfaEnrollmentTtl, resetPasswordTtl, emailVerificationTtl)
        )


}
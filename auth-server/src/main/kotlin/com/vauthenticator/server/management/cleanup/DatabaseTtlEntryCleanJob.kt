package com.vauthenticator.server.management.cleanup

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForList
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Transactional
class DatabaseTtlEntryCleanJob(
    private val jdbcTemplate: JdbcTemplate,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(DatabaseTtlEntryCleanJob::class.java)

    fun execute() {
        logger.info("Job Running")
        val now = clock.instant().epochSecond

        deleteOldTicket(now)
        deleteOldKeys(now)
        logger.info("Job Completed")

    }

    private fun deleteOldKeys(now: Long) {
        val keysToBeDeleted =
            jdbcTemplate.queryForList(
                "SELECT key_id,key_purpose  FROM KEYS WHERE key_expiration_date_timestamp < ?",
                now
            )
        keysToBeDeleted.forEach {
            jdbcTemplate.update(
                "DELETE FROM KEYS WHERE  key_id = ? AND key_purpose = ?;", it["key_id"], it["key_purpose"]
            )
        }
    }

    private fun deleteOldTicket(now: Long) {
        val ticketToBeDeleted =
            jdbcTemplate.queryForList<String>(
                "SELECT ticket FROM TICKET WHERE ttl < ?",
                arrayOf(now)
            )
        ticketToBeDeleted.forEach {
            jdbcTemplate.update("DELETE FROM TICKET WHERE ticket = ?", it)
        }
    }

}


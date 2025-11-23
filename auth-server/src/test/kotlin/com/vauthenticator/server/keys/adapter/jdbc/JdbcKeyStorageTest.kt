package com.vauthenticator.server.keys.adapter.jdbc

import com.vauthenticator.server.keys.adapter.AbstractKeyStorageTest
import com.vauthenticator.server.keys.domain.KeyPurpose
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb


class JdbcKeyStorageTest : AbstractKeyStorageTest() {

    override fun initKeyStorage(): KeyStorage = JdbcKeyStorage(jdbcTemplate, clock())

    override fun resetDatabase() {
        resetDb()
    }

    override fun getActual(kid: Kid, keyPurpose: KeyPurpose): Map<String, Any> {
        val query = jdbcTemplate.query(
            "SELECT * FROM KEYS WHERE key_id = ? AND key_purpose = ?;", { rs, _ ->
                mapOf(
                    "key_id" to rs.getString("key_id"),
                    "master_key_id" to rs.getString("master_key_id"),
                    "encrypted_private_key" to rs.getString("encrypted_private_key"),
                    "public_key" to rs.getString("public_key"),
                    "key_expiration_date_timestamp" to rs.getLong("key_expiration_date_timestamp"),
                    "enabled" to rs.getBoolean("enabled"),
                )
            },
            kid.content(), keyPurpose.name
        )
        return if (query.isNotEmpty()) {
            query.first()
        } else {
            emptyMap()
        }
    }

}
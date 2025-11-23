package com.vauthenticator.server.keys.adapter.jdbc

import com.vauthenticator.server.extentions.expirationTimeStampInSecondFromNow
import com.vauthenticator.server.keys.domain.*
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet
import java.time.Clock
import java.time.Duration

class JdbcKeyStorage(
    private val jdbcTemplate: JdbcTemplate,
    private val clock: Clock,
    ) : KeyStorage {

    private val logger = LoggerFactory.getLogger(JdbcKeyStorage::class.java)

    override fun store(
        masterKid: MasterKid,
        kid: Kid,
        dataKey: DataKey,
        keyType: KeyType,
        keyPurpose: KeyPurpose
    ) {
        jdbcTemplate.update(
            """
                INSERT INTO KEYS(
                    key_id,
                    master_key_id,
                    key_purpose,
                    key_type,
                    encrypted_private_key,
                    public_key,
                    enabled,
                    key_expiration_date_timestamp
                ) VALUES (?,?,?,?,?,?,?,?)
                """.trimIndent(),
            kid.content(),
            masterKid.content(),
            keyPurpose.name,
            keyType.name,
            dataKey.encryptedPrivateKeyAsString(),
            dataKey.publicKeyAsString(),
            true,
            Duration.ofSeconds(0).toSeconds()
        )
    }

    override fun signatureKeys(): Keys = Keys(
        jdbcTemplate.query(
            "SELECT * FROM KEYS WHERE key_purpose = ?;", { rs, _ -> keyFrom(rs) },
            KeyPurpose.SIGNATURE.name
        )
    )


    override fun findOne(kid: Kid, keyPurpose: KeyPurpose): Key {
        return jdbcTemplate.query(
            "SELECT * FROM KEYS WHERE key_id = ? AND key_purpose = ?;", { rs, _ -> keyFrom(rs) },
            kid.content(), keyPurpose.name
        ).first()
    }

    private fun keyFrom(rs: ResultSet) = Key(
        kid = Kid(rs.getString("key_id")),
        masterKid = MasterKid(rs.getString("master_key_id")),
        keyPurpose = KeyPurpose.valueOf(rs.getString("key_purpose")),
        enabled = rs.getBoolean("enabled"),
        type = KeyType.valueOf(rs.getString("key_type")),
        expirationDateTimestamp = rs.getLong("key_expiration_date_timestamp"),
        dataKey = DataKey.from(
            rs.getString("encrypted_private_key"),
            rs.getString("public_key"),
        )
    )

    override fun justDeleteKey(kid: Kid, keyPurpose: KeyPurpose) {
        jdbcTemplate.update(
            "DELETE FROM KEYS WHERE  key_id = ? AND key_purpose = ?;", kid.content(), keyPurpose.name
        )
    }

    override fun keyDeleteJodPlannedFor(kid: Kid, ttl: Duration, keyPurpose: KeyPurpose) {
            jdbcTemplate.update("UPDATE KEYS SET key_expiration_date_timestamp=?, enabled=? WHERE key_id =? AND key_expiration_date_timestamp=0 AND key_purpose=?",
                ttl.expirationTimeStampInSecondFromNow(clock), false, kid.content(), keyPurpose.name
            )
    }

}
package com.vauthenticator.server.mfa.adapter.jdbc

import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.mfa.domain.MfaAccountMethod
import com.vauthenticator.server.mfa.domain.MfaAccountMethodsRepository
import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.mfa.domain.MfaMethod
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.*

@Transactional
class JdbcMfaAccountMethodsRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val keyRepository: KeyRepository,
    private val masterKid: MasterKid,
    private val mfaDeviceIdGenerator: () -> MfaDeviceId
) : MfaAccountMethodsRepository {
    override fun findBy(userName: String, mfaMfaMethod: MfaMethod, mfaChannel: String): Optional<MfaAccountMethod> =
        Optional.ofNullable(
            jdbcTemplate.query(
                "SELECT * FROM MFA_ACCOUNT_METHODS WHERE user_name=? AND mfa_method=? AND mfa_channel=?",
                { rs, _ -> mfaAccountMethodFrom(rs) },
                userName, mfaMfaMethod.name, mfaChannel
            ).firstOrNull()
        )


    override fun findBy(deviceId: MfaDeviceId): Optional<MfaAccountMethod> =
        Optional.ofNullable(
            jdbcTemplate.query(
                "SELECT * FROM MFA_ACCOUNT_METHODS WHERE mfa_device_id=?",
                { rs, _ -> mfaAccountMethodFrom(rs) },
                deviceId.content
            ).firstOrNull()
        )

    override fun findAll(userName: String): List<MfaAccountMethod> =
        jdbcTemplate.query("SELECT * FROM MFA_ACCOUNT_METHODS")
        { rs, _ -> mfaAccountMethodFrom(rs) }


    override fun save(
        userName: String,
        mfaMfaMethod: MfaMethod,
        mfaChannel: String,
        associated: Boolean
    ): MfaAccountMethod {
        val (kid, mfaDeviceId) = findBy(userName, mfaMfaMethod, mfaChannel)
            .map {
                listOf(it.key, it.mfaDeviceId)
            }.orElseGet {
                val kid = keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA)
                val mfaDeviceId = mfaDeviceIdGenerator.invoke()
                listOf(kid, mfaDeviceId)
            }

        jdbcTemplate.update(
            """INSERT INTO MFA_ACCOUNT_METHODS (user_name, mfa_device_id, mfa_method, mfa_channel, key_id, associated) VALUES (?,?,?,?,?,?) 
                    ON CONFLICT(user_name, mfa_channel) 
                    DO UPDATE SET mfa_device_id=?,
                                mfa_method=?,
                                key_id=?,
                                associated=?
                                """,
            userName,
            (mfaDeviceId as MfaDeviceId).content,
            mfaMfaMethod.name,
            mfaChannel,
            (kid as Kid).content(),
            associated,
            mfaDeviceId.content,
            mfaMfaMethod.name,
            kid.content(),
            associated
            )

        return MfaAccountMethod(userName, mfaDeviceId, kid, mfaMfaMethod, mfaChannel, associated)
    }


    override fun setAsDefault(userName: String, deviceId: MfaDeviceId) {
        Optional.ofNullable(
            jdbcTemplate.query(
                "SELECT mfa_device_id FROM MFA_ACCOUNT_METHODS WHERE user_name=? AND default_mfa_method=true",
                { rs, _ -> MfaDeviceId(rs.getString("mfa_device_id")) },
                userName
            ).firstOrNull()
        ).ifPresent {
            jdbcTemplate.update(
                "UPDATE MFA_ACCOUNT_METHODS SET default_mfa_method = false WHERE  user_name=? AND mfa_device_id=?",
                userName, it.content
            )
        }

        jdbcTemplate.update(
            "UPDATE MFA_ACCOUNT_METHODS SET default_mfa_method = true WHERE  user_name=? AND mfa_device_id=?",
            userName, deviceId.content
        )
    }

    override fun getDefaultDevice(userName: String): Optional<MfaDeviceId> =
        Optional.ofNullable(
            jdbcTemplate.query(
                "SELECT mfa_device_id FROM MFA_ACCOUNT_METHODS WHERE user_name=? AND default_mfa_method=true",
                { rs, _ -> MfaDeviceId(rs.getString("mfa_device_id")) },
                userName
            ).firstOrNull()
        )

    private fun mfaAccountMethodFrom(rs: ResultSet) = MfaAccountMethod(
        userName = rs.getString("user_name"),
        mfaDeviceId = MfaDeviceId(rs.getString("mfa_device_id")),
        key = Kid(rs.getString("key_id")),
        mfaMethod = MfaMethod.valueOf(rs.getString("mfa_method")),
        mfaChannel = rs.getString("mfa_channel"),
        associated = rs.getBoolean("associated"),
    )

}
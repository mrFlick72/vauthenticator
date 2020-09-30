package it.valeriovaudi.vauthenticator.account

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.*

interface AccountRepository {
    fun findAll(): List<Account>
    fun accountFor(username: String): Optional<Account>
    fun save(account: Account)
}

class AccountRegistrationException(e: RuntimeException) : RuntimeException(e)

val roleLoader: (ResultSet, Int) -> String = { rs, _ -> rs.getString("role") }
val accountLoader: (ResultSet, Int) -> Account = { rs, _ ->
    Account(
            accountNonExpired = rs.getBoolean("account_non_expired"),
            accountNonLocked = rs.getBoolean("account_non_locked"),
            credentialsNonExpired = rs.getBoolean("credentials_non_expired"),
            enabled = rs.getBoolean("enabled"),

            username = rs.getString("username"),
            password = rs.getString("password"),
            authorities = emptyList(),

            // needed for email oidc profile
            email = rs.getString("email"),
            emailVerified = rs.getBoolean("email_verified"),

            // needed for profile oidc profile
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name")
    )
}
const val findAllAccountRoleFor: String = "SELECT * FROM account_role where username=?"
const val findAll: String = "SELECT * FROM account"
const val readByEmail: String = "SELECT * FROM account WHERE email=?"
val insertQuery: String =
        """
                INSERT INTO ACCOUNT (account_non_expired,
                                     account_non_locked,
                                     credentials_non_expired,
                                     enabled,
                                     username,
                                     password,
                                     email,
                                     email_verified,
                                     first_name,
                                     last_name
                                    ) 
                                    VALUES (?,?,?,?,?,?,?,?,?,?)
                                    ON CONFLICT (email) DO UPDATE SET
                                     account_non_expired=?,
                                     account_non_locked=?,
                                     credentials_non_expired=?,
                                     enabled=?,
                                     password=?,
                                     email_verified=?,
                                     first_name=?,
                                     last_name=?
            """.trimIndent()


@Transactional
class JdbcAccountRepository(private val jdbcTemplate: JdbcTemplate) : AccountRepository {

    @Transactional(readOnly = true)
    override fun findAll(): List<Account> =
            jdbcTemplate.query(findAll, accountLoader)
                    .map { it.copy(authorities = jdbcTemplate.query(findAllAccountRoleFor, roleLoader, arrayOf(it.email))) }

    @Transactional(readOnly = true)
    override fun accountFor(username: String): Optional<Account> {
        return jdbcTemplate.query(findAllAccountRoleFor, roleLoader, arrayOf(username))
                .let { roles ->
                    Optional.ofNullable(
                            jdbcTemplate.queryForObject(readByEmail, accountLoader, arrayOf(username))
                    ).map { it.copy(authorities = roles) }
                }
    }

    override fun save(account: Account) {
        jdbcTemplate.update(insertQuery,
                account.accountNonExpired, account.accountNonLocked, account.credentialsNonExpired, account.enabled,
                account.email, account.password,
                account.email, account.emailVerified, account.firstName, account.lastName,

                account.accountNonExpired, account.accountNonLocked, account.credentialsNonExpired, account.enabled,
                account.password, account.emailVerified, account.firstName, account.lastName
        )

        account.authorities
                .forEach { jdbcTemplate.update("INSERT INTO ACCOUNT_ROLE(USERNAME, ROLE) VALUES (?,?)", account.username, it) }
    }

}
package com.vauthenticator.server.account.adapter.jdbc

import com.vauthenticator.server.account.domain.*
import com.vauthenticator.server.account.domain.Date
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val SAVE_QUERY: String = """
        INSERT INTO Account (
            account_non_expired,
            account_non_locked,
            credentials_non_expired,
            enabled,
            username,
            password,
            email,
            email_verified,
            first_name,
            last_name,
            birth_date,
            phone,
            locale,
            mandatory_action
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)  ON CONFLICT(username) DO UPDATE SET account_non_expired=?,
            account_non_locked=?,
            credentials_non_expired=?,
            enabled=?,
            password=?,
            email=?,
            email_verified=?,
            first_name=?,
            last_name=?,
            birth_date=?,
            phone=?,
            locale=?,
            mandatory_action=?
    """
private const val CREATE_QUERY: String = """
        INSERT INTO Account (
            account_non_expired,
            account_non_locked,
            credentials_non_expired,
            enabled,
            username,
            password,
            email,
            email_verified,
            first_name,
            last_name,
            birth_date,
            phone,
            locale,
            mandatory_action
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) 
    """

private const val FIND_ONE_QUERY: String = """
    SELECT account_non_expired,
           account_non_locked,
           credentials_non_expired,
           enabled,
           username,
           password,
           email,
           email_verified,
           first_name,
           last_name,
           birth_date,
           phone,
           locale,
           mandatory_action
     FROM Account
     WHERE username=?
    """

private const val FIND_ACCOUNT_ROLE_QUERY: String = """
    SELECT role_name
     FROM ACCOUNT_ROLE
     WHERE account_username=?
    """
private const val DELETE_ACCOUNT_ROLE_QUERY = "DELETE FROM ACCOUNT_ROLE WHERE role_name=?"
private const val INSERT_ACCOUNT_ROLE_QUERY = "INSERT INTO ACCOUNT_ROLE (account_username, role_name) VALUES (?,?)"

private const val FIND_ACCOUNT_GROUP_QUERY: String = """
    SELECT role_name
     FROM ACCOUNT_GROUP
     WHERE account_username=?
    """
private const val DELETE_ACCOUNT_GROUP_QUERY = "DELETE FROM ACCOUNT_GROUP WHERE group_name=?"
private const val INSERT_ACCOUNT_GROUP_QUERY = "INSERT INTO ACCOUNT_GROUP (account_username, group_name) VALUES (?,?)"

@Transactional
class JdbcAccountRepository(private val jdbcTemplate: JdbcTemplate) : AccountRepository {


    @Transactional(readOnly = true)
    override fun accountFor(username: String): Optional<Account> {

        val authorities: Set<String> = getUserRoleFor(username)
        val groups: Set<String> = getUserGroupFor(username)

        val queryResult = jdbcTemplate.query(FIND_ONE_QUERY, { rs, _ ->
            Account(
                accountNonExpired = rs.getBoolean("account_non_expired"),
                accountNonLocked = rs.getBoolean("account_non_locked"),
                credentialsNonExpired = rs.getBoolean("credentials_non_expired"),
                enabled = rs.getBoolean("enabled"),
                username = rs.getString("username"),
                password = rs.getString("password"),
                email = rs.getString("email"),
                emailVerified = rs.getBoolean("email_verified"),
                firstName = rs.getString("first_name"),
                lastName = rs.getString("last_name"),
                authorities = authorities,
                birthDate = Date.isoDateFor(
                    rs.getString("birth_date").orEmpty()
                ),
                phone = Phone.phoneFor(rs.getString("phone")),
                locale = UserLocale.localeFrom(
                    rs.getString(
                        "locale"
                    )
                ),
                mandatoryAction = AccountMandatoryAction.valueOf(
                    rs.getString("mandatory_action")
                ),
                groups = groups

            )
        }, username)
        return Optional.ofNullable(queryResult.firstOrNull())
    }

    private fun getUserRoleFor(username: String) =
        jdbcTemplate.query(FIND_ACCOUNT_ROLE_QUERY, { rs, _ -> rs.getString("role_name") }, username).toSet()

    private fun getUserGroupFor(username: String): Set<String> =
        jdbcTemplate.query(FIND_ACCOUNT_GROUP_QUERY, { rs, _ -> rs.getString("group_name") }, username).toSet()

    override fun save(account: Account) {
        jdbcTemplate.update(
            SAVE_QUERY,
            account.accountNonExpired,
            account.accountNonLocked,
            account.accountNonExpired,
            account.enabled,
            account.username,
            account.password,
            account.email,
            account.emailVerified,
            account.firstName,
            account.lastName,
            account.birthDate.map { it.localDate }.orElse(null),
            account.phone.map { it.formattedPhone() }.orElse(""),
            account.locale.map { it.formattedLocale() }.orElse(""),
            account.mandatoryAction.name,


            account.accountNonExpired,
            account.accountNonLocked,
            account.accountNonExpired,
            account.enabled,
            account.password,
            account.email,
            account.emailVerified,
            account.firstName,
            account.lastName,
            account.birthDate.map { it.localDate }.orElse(null),
            account.phone.map { it.formattedPhone() }.orElse(""),
            account.locale.map { it.formattedLocale() }.orElse(""),
            account.mandatoryAction.name
        )

        saveRoleFor(account.username, account.authorities)
        saveGroupFor(account.username, account.groups)
    }

    override fun create(account: Account) {
        try {
            jdbcTemplate.update(
                CREATE_QUERY,
                account.accountNonExpired,
                account.accountNonLocked,
                account.accountNonExpired,
                account.enabled,
                account.username,
                account.password,
                account.email,
                account.emailVerified,
                account.firstName,
                account.lastName,
                account.birthDate.map { it.localDate }.orElse(null),
                account.phone.map { it.formattedPhone() }.orElse(""),
                account.locale.map { it.formattedLocale() }.orElse(""),
                account.mandatoryAction.name
            )
            saveRoleFor(account.username, account.authorities)
            saveGroupFor(account.username, account.groups)
        } catch (e: DuplicateKeyException) {
            throw AccountRegistrationException(e.message!!, e)
        }
    }

    private fun saveRoleFor(userName: String, roles: Set<String>) {
        val userRoles: Set<String> = getUserRoleFor(userName)

        userRoles.forEach { jdbcTemplate.update(DELETE_ACCOUNT_ROLE_QUERY, it) }
        roles.forEach {
            jdbcTemplate.update(INSERT_ACCOUNT_ROLE_QUERY, userName, it)
        }
    }

    private fun saveGroupFor(userName: String, groups: Set<String>) {
        val userRoles: Set<String> = getUserRoleFor(userName)

        userRoles.forEach { jdbcTemplate.update(DELETE_ACCOUNT_GROUP_QUERY, it) }
        groups.forEach {
            jdbcTemplate.update(INSERT_ACCOUNT_GROUP_QUERY, userName, it)
        }
    }

}
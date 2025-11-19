package com.vauthenticator.server.account.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.extentions.toSha256
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class Account(
    var accountNonExpired: Boolean = false,
    var accountNonLocked: Boolean = false,
    var credentialsNonExpired: Boolean = false,
    var enabled: Boolean = false,

    var username: String,
    var password: String,
    var authorities: Set<String>,
    var groups: Set<String>,

    var email: String,
    var emailVerified: Boolean = false,

    var firstName: String,
    var lastName: String,

    val birthDate: Optional<Date>,
    val phone: Optional<Phone>,
    val locale: Optional<UserLocale>,
    val mandatoryAction: AccountMandatoryAction
) {
    val sub: String
        get() = email.toSha256()
}

enum class AccountMandatoryAction {

    NO_ACTION,
    RESET_PASSWORD
}

@JvmInline
value class Email(val content: String)

data class UserLocale(val locale: Locale) {
    fun formattedLocale(): String = this.locale.toLanguageTag()

    companion object {
        fun localeFrom(lang: String) = try {
            val locale = Locale.forLanguageTag(lang)
            if (locale.toLanguageTag() == "und") {
                empty()
            } else {
                Optional.of(UserLocale(locale))
            }
        } catch (e: Exception) {
            empty()
        }

        fun empty(): Optional<UserLocale> =
            Optional.empty()

    }
}

data class Date(
    val localDate: LocalDate,
    val dateTimeFormatter: DateTimeFormatter = USER_INFO_DEFAULT_DATE_TIME_FORMATTER
) : Comparable<Date> {

    fun formattedDate(): String {
        return dateTimeFormatter.format(localDate)
    }

    fun iso8601FormattedDate(): String {
        return USER_INFO_DEFAULT_DATE_TIME_FORMATTER.format(localDate)
    }

    override operator fun compareTo(o: Date): Int {
        return this.localDate.compareTo(o.localDate)
    }

    companion object {
        val USER_INFO_DEFAULT_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fun empty(): Optional<Date> = Optional.empty()

        fun isoDateFor(date: String): Optional<Date> = try {
            Optional.of(
                Date(
                    LocalDate.parse(
                        date,
                        USER_INFO_DEFAULT_DATE_TIME_FORMATTER
                    )
                )
            )
        } catch (e: RuntimeException) {
            empty()
        }

    }
}

data class Phone(private val countryPrefix: String, private val prefix: String, private val phoneNumber: String) {
    fun formattedPhone(): String {
        return String.format("%s %s %s", countryPrefix, prefix, phoneNumber).trim { it <= ' ' }
    }

    companion object {
        fun nullValue(): Phone {
            return Phone("", "", "")
        }

        fun phoneFor(phoneNumber: String): Optional<Phone> = try {
            var phone = Optional.empty<Phone>()
            val split = phoneNumber.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (split.size == 3) {
                phone = Optional.of(
                    Phone(
                        split[0],
                        split[1],
                        split[2]
                    )
                )
            } else if (split.size == 2) {
                phone = Optional.of(
                    Phone(
                        "",
                        split[0],
                        split[1]
                    )
                )
            }
            phone
        } catch (e: RuntimeException) {
            empty()
        }

        fun empty(): Optional<Phone> = Optional.empty()
    }
}


class AccountNotFoundException(message: String) : RuntimeException(message)

class AccountCacheContentConverter(private val objectMapper: ObjectMapper) : CacheContentConverter<Account> {
    override fun getObjectFromCacheContentFor(cacheContent: String): Account =
        objectMapper.readValue(cacheContent, Map::class.java)
            .let {
                Account(
                    accountNonExpired = it["credentialsNonExpired"] as Boolean,
                    accountNonLocked = it["credentialsNonExpired"] as Boolean,
                    credentialsNonExpired = it["credentialsNonExpired"] as Boolean,
                    enabled = it["enabled"] as Boolean,
                    username = it["username"] as String,
                    password = it["password"] as String,
                    authorities = (it["authorities"] as List<String>).toSet(),
                    groups = (it["groups"] as List<String>).toSet(),
                    email = it["email"] as String,
                    emailVerified = it["emailVerified"] as Boolean,
                    firstName = it["firstName"] as String,
                    lastName = it["lastName"] as String,
                    birthDate = Date.isoDateFor(it["birthDate"] as String),
                    phone = Phone.phoneFor(it["phone"] as String),
                    locale = UserLocale.localeFrom(
                        (it["locale"] as String)
                    ),
                    mandatoryAction = AccountMandatoryAction.valueOf(
                        it["mandatory_action"] as String
                    )
                )
            }


    override fun loadableContentIntoCacheFor(source: Account): String =
        objectMapper.writeValueAsString(
            mapOf(
                "accountNonExpired" to source.accountNonExpired,
                "accountNonLocked" to source.accountNonLocked,
                "credentialsNonExpired" to source.credentialsNonExpired,
                "enabled" to source.enabled,
                "username" to source.username,
                "password" to source.password,
                "authorities" to source.authorities,
                "groups" to source.groups,
                "email" to source.email,
                "emailVerified" to source.emailVerified,
                "firstName" to source.firstName,
                "lastName" to source.lastName,
                "birthDate" to source.birthDate.map { it.iso8601FormattedDate() }.orElseGet { "" },
                "phone" to source.phone.map { it.formattedPhone() }.orElseGet { "" },
                "locale" to source.locale.map { it.formattedLocale() }.orElseGet { "" },
                "mandatory_action" to source.mandatoryAction.name
            )
        )

}
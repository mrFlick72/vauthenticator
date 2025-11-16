package com.vauthenticator.server.oidc.userinfo

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext
import java.time.LocalDateTime
import java.time.ZoneOffset


open class UserInfoEnhancer(private val accountRepository: AccountRepository) {

    open fun oidcUserInfoFrom(principal: OidcUserInfoAuthenticationContext): OidcUserInfo =
        accountRepository.accountFor(userName(principal))
            .map { account ->
                val claims = mutableMapOf<String, Any>()

                RoleClaimsProvider(account, claims)
                OpenIdClaimsProvider(account, claims)
                EmailClaimsProvider(account, claims)
                ProfileClaimsProvider(account, claims)
                OidcUserInfo(claims)
            }
            .orElseThrow()

}


typealias ClaimsProvider = (Account, MutableMap<String, Any>) -> MutableMap<String, Any>


object RoleClaimsProvider : ClaimsProvider {
    override fun invoke(account: Account, claims: MutableMap<String, Any>): MutableMap<String, Any> {
        claims["authorities"] = account.authorities
        return claims
    }

}

object EmailClaimsProvider : ClaimsProvider {
    override fun invoke(account: Account, claims: MutableMap<String, Any>): MutableMap<String, Any> {
        claims["email"] = account.email
        claims["email_verified"] = true
        return claims
    }

}


object ProfileClaimsProvider : ClaimsProvider {
    override fun invoke(account: Account, claims: MutableMap<String, Any>): MutableMap<String, Any> {
        claims["name"] = "${account.firstName} ${account.lastName}"
        claims["given_name"] = account.firstName
        claims["family_name"] = account.lastName
        claims["middle_name"] = ""
        claims["updated_at"] = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

        account.phone.ifPresent {
            claims["phone_number"] = it.formattedPhone()
            claims["phone_number_verified"] = true
        }
        account.birthDate.ifPresent {
            claims["birthdate"] = it.iso8601FormattedDate()
        }
        account.locale.ifPresent {
            claims["locale"] = it.formattedLocale()
        }
        return claims
    }

}

object OpenIdClaimsProvider : ClaimsProvider {
    override fun invoke(account: Account, claims: MutableMap<String, Any>): MutableMap<String, Any> {
        claims["sub"] = account.email
        return claims
    }

}

fun userName(principal: OidcUserInfoAuthenticationContext) =
    principal.authorization.accessToken.claims!!["sub"] as String

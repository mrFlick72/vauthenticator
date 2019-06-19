package it.valeriovaudi.vauthenticator.security

import it.valeriovaudi.vauthenticator.integration.LogInRequestGateway
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class AccountUserDetailsService(private val logInRequestGateway: LogInRequestGateway) : UserDetailsService {

    override fun loadUserByUsername(username: String) =
            try {
                logInRequestGateway.getPrincipleByUserName(username)
            } catch (e: Exception) {
                throw UsernameNotFoundException(e.message, e)
            }

}
package it.valeriovaudi.vauthenticator.testableaccountservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.integration.dsl.MessageChannels
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootApplication
class TestableAccountServiceApplication {

    @Bean
    fun userDetailsProcessorChannelHandler(objectMapper: ObjectMapper) =
            UserDetailsProcessorChannelHandler(scurityAccountUser(objectMapper))

    @Bean
    fun accountRepository() =
            InMemoryAccountRepository(mapOf("user" to Account(userName = "user",
                    password = "secret",
                    firstName = "Vauthenticator",
                    lastName = "Vaudi",
                    mail = "valerio.vaudi@gmail.com",
                    telephone = "xx-xxxx-xxxxx")))

    @Bean
    fun scurityAccountUser(objectMapper: ObjectMapper) = ScurityAccountUser(accountRepository(), objectMapper)

}

fun main(args: Array<String>) {
    runApplication<TestableAccountServiceApplication>(*args)
}


data class Account(val userName: String,
                   val password: String,
                   val firstName: String,
                   val lastName: String,
                   val mail: String,
                   val telephone: String)

interface AccountRepository {
    fun findByUserName(userName: String): Optional<Account>
// ... other methods are useless for this account service testable implentation
}

class InMemoryAccountRepository(val inMemoryStorage: Map<String, Account> = emptyMap()) : AccountRepository {
    override fun findByUserName(userName: String) = Optional.ofNullable(inMemoryStorage[userName])
}

@RestController
class AccountEndPoint(private val accountRepository: AccountRepository) {

    @GetMapping("/account/{userName}")
    fun findAccount(@PathVariable userName: String) =
            accountRepository.findByUserName(userName).map { ok(it) }
                    .orElse(notFound().build())
}


@EnableWebSecurity
class SecurityOAuth2ResourceServerConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .authorizeRequests().anyRequest().authenticated().and()
                .oauth2ResourceServer().jwt()
    }

}


class UserDetailsProcessorChannelHandler(private val accountUserDetailsService: ScurityAccountUser) {

    @RabbitListener(queues = ["authServerAccountServiceBridgeInboundQueue"])
    fun getUserDetails(userName: String): String? {
        return accountUserDetailsService.loadUserByUsername(userName)
    }

}

class ScurityAccountUser(private val accountRepository: AccountRepository, private val objectMapper: ObjectMapper) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun loadUserByUsername(userName: String) =
            accountRepository.findByUserName(userName)
                    .map {

                        objectMapper.writeValueAsString(SecurityAccountDetails(it.userName,
                                passwordEncoder.encode(it.password),
                                listOf("USER", "ADMIN")))
                    }
                    .orElseThrow({ UsernameNotFoundException("user not found") })

}

data class SecurityAccountDetails(var username: String, var password: String, var authorities: List<String>)

@Configuration
class MessagingConfig {

    @Bean("authServerAccountServiceBridgeInboundChannel")
    fun authServerAccountServiceBridgeInboundChannel() =
            MessageChannels.direct().get()

    @Bean("authServerAccountServiceBridgeOutboundChannel")
    fun authServerAccountServiceBridgeOutboundChannel() =
            MessageChannels.direct().get()


    @Bean("authServerAccountServiceBridgeInboundQueue")
    fun authServerAccountServiceBridgeInboundQueue() =
            Queue("authServerAccountServiceBridgeInboundQueue", false)


    @Bean("authServerAccountServiceBridgeOutboundQueue")
    fun authServerAccountServiceBridgeOutboundQueue() =
            Queue("authServerAccountServiceBridgeOutboundQueue", false)

}

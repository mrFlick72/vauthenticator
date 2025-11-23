package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallengeUponSignUpEventConsumer
import com.vauthenticator.server.account.domain.signup.SignUpEventConsumer
import com.vauthenticator.server.account.domain.welcome.SendWelcomeMailUponSignUpEventConsumer
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordEventConsumer
import com.vauthenticator.server.password.domain.resetpassword.ResetPasswordEventConsumer
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class EventsConfig(private val eventConsumerConfig: EventConsumerConfig) {

    @Bean
    fun eventsDispatcherAdapter(publisher: ApplicationEventPublisher) =
        SpringEventEventsDispatcher(publisher)

    @Bean
    fun eventsDispatcher(publisher: ApplicationEventPublisher) =
        VAuthenticatorEventsDispatcher(publisher)

    @Bean
    fun eventsCollector(
        changePasswordEventConsumer: ChangePasswordEventConsumer,
        resetPasswordEventConsumer: ResetPasswordEventConsumer,
        signUpEventConsumer: SignUpEventConsumer,
        sendWelcomeMailUponSignUpEventConsumer: SendWelcomeMailUponSignUpEventConsumer,
        sendVerifyEMailChallengeUponSignUpEventConsumer: SendVerifyEMailChallengeUponSignUpEventConsumer,
        loggerEventConsumer: EventConsumer
    ) =
        SpringEventsCollector(
            listOf(
                loggerEventConsumer,
                sendWelcomeMailUponSignUpEventConsumer,
                sendVerifyEMailChallengeUponSignUpEventConsumer,
                signUpEventConsumer,
                resetPasswordEventConsumer,
                changePasswordEventConsumer
            )
        )

    @Bean
    fun loggerEventConsumer() = LoggerEventConsumer(eventConsumerConfig)

}
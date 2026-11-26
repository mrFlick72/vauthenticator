package com.vauthenticator.server.events

import com.vauthenticator.platform.events.EventConsumer
import com.vauthenticator.platform.events.VAuthenticatorEvent
import com.vauthenticator.platform.events.ConsumingEventsCollector
import com.vauthenticator.platform.events.EventConsumerConfig
import com.vauthenticator.platform.events.LoggingEventConsumer

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
        loggerEventConsumer: EventConsumer<VAuthenticatorEvent>
    ) =
        ConsumingEventsCollector<VAuthenticatorEvent>(
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
    fun loggerEventConsumer() = LoggingEventConsumer<VAuthenticatorEvent>(eventConsumerConfig)

}
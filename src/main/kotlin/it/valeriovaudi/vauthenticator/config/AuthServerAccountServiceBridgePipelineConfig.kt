package it.valeriovaudi.vauthenticator.config

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlows

@Configuration
class AuthServerAccountServiceBridgePipelineConfig(private val rabbitTemplate: RabbitTemplate) {

    @Bean
    fun getUserDetailsIntegrationPipelineConfig(authServerAccountServiceBridgeInboundChannel: DirectChannel,
                                                authServerAccountServiceBridgeOutboundChannel: DirectChannel) =
            IntegrationFlows.from(authServerAccountServiceBridgeInboundChannel)
                    .handle<AmqpOutboundEndpoint>(Amqp.outboundGateway(rabbitTemplate)
                            .routingKey("authServerAccountServiceBridgeInboundQueue")
                            .returnChannel(authServerAccountServiceBridgeOutboundChannel))
                    .get()

}

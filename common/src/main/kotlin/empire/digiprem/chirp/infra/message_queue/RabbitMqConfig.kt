@file:Suppress("Deprecation")

package empire.digiprem.chirp.infra.message_queue

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import empire.digiprem.chirp.domain.ChirpEvent
import empire.digiprem.chirp.domain.events.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {

        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            findAndRegisterModules()
            val polymorphicModule = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(ChirpEvent::class.java)
                .allowIfSubType("java.util.")
                .allowIfBaseType("kotlin.collections.")
                .build()

            activateDefaultTyping(polymorphicModule, ObjectMapper.DefaultTyping.NON_FINAL)
        }

        return Jackson2JsonMessageConverter(objectMapper).apply {
            typePrecedence= Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: Jackson2JsonMessageConverter,
    ): RabbitTemplate{
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange()= TopicExchange(
        UserEventConstants.USER_EXCHANGE,
        true,
        false
    )

    @Bean
    fun notificationUserEventsQueue()= Queue(
        MessageQueues.NOTIFICATION_USER_EVENTS,
        true
    )

    @Bean
    fun notificationUserEventsBinding(
        notificationEventQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue())
            .to(userExchange)
            .with("user.*")
    }
}
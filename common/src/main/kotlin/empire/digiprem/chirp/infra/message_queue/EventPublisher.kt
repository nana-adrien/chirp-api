package empire.digiprem.chirp.infra.message_queue

import empire.digiprem.chirp.domain.ChirpEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)



    fun <T: ChirpEvent> publish(event:T){
        try {
            rabbitTemplate.convertAndSend(
                event.exchange,
                event.eventKey,
                event
            )
            logger.info("successfully published event: ${event.eventId} ")
        }catch (e:Exception){
            logger.error("failed to publish ${event.eventKey} event", e)
        }
    }
}
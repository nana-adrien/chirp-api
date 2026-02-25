package empire.digiprem.empire.digiprem.chirp.infra.message_queue

import empire.digiprem.chirp.infra.message_queue.MessageQueues
import empire.digiprem.empire.digiprem.chirp.domain.events.user.UserEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationUserEventListener {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    fun handleUserEvent(event: UserEvent){
        when(event){
            is UserEvent.Created -> {
                println("Received User Created Event for user: ${event.username}, email: ${event.email}")
            }
            is UserEvent.RequestResendVerification ->{
                println("Received User Resend Verification Event for user: ${event.username}")
            }
            is UserEvent.RequestResetPassword -> {
                println("Received User Reset Password Event for user: ${event.username}")
            }
            is UserEvent.Verified -> {
                println("Received User Verified Event for user: ${event.username}")
            }
        }
    }

}
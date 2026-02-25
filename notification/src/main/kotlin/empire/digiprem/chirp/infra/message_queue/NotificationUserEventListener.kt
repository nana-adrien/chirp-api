package empire.digiprem.chirp.infra.message_queue

import empire.digiprem.chirp.domain.events.user.UserEvent
import empire.digiprem.chirp.services.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class NotificationUserEventListener (
   private val emailService: EmailService
){

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    fun handleUserEvent(event: UserEvent){
        when(event){
            is UserEvent.Created -> {
                emailService.sendVerificationEmail(
                    email = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.verificationToken,
                )
            }
            is UserEvent.RequestResendVerification ->{
                println("Received User Resend Verification Event for user: ${event.username}")

            }
            is UserEvent.RequestResetPassword -> {

                emailService.sendPasswordResetEmail(
                    email = event.email,
                    username = event.username,
                    userId = event.userId,
                    token = event.passwordResetToken,
                    expiresIn = Duration.ofMinutes(event.expiresInMinute)
                )
            }
            is UserEvent.Verified -> {
                println("Received User Verified Event for user: ${event.username}")

            }
        }
    }

}
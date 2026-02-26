package empire.digiprem.chirp.infra.messages

import empire.digiprem.chirp.domain.events.user.UserEvent
import empire.digiprem.chirp.domain.models.ChatParticipant
import empire.digiprem.chirp.infra.message_queue.MessageQueues
import empire.digiprem.chirp.service.ChatParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener (
    private val chatParticipantService: ChatParticipantService
){

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handlerUserEvent(event: UserEvent){
        when (event) {
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null
                    )
                )
            }
            else -> Unit
        }
    }
}
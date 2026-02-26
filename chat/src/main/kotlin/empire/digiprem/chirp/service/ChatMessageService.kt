package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.event.MessageDeletedEvent
import empire.digiprem.chirp.domain.events.chat.ChatEvent
import empire.digiprem.chirp.domain.exception.ChatNotFoundException
import empire.digiprem.chirp.domain.exception.ChatParticipantNotFoundException
import empire.digiprem.chirp.domain.exception.MessageNotFoundException
import empire.digiprem.chirp.domain.models.ChatMessage
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.exception.ForbiddenException
import empire.digiprem.chirp.infra.database.entities.ChatMessageEntity
import empire.digiprem.chirp.infra.database.mappers.toChatMessage
import empire.digiprem.chirp.infra.database.repositories.ChatMessageRepository
import empire.digiprem.chirp.infra.database.repositories.ChatParticipantRepository
import empire.digiprem.chirp.infra.database.repositories.ChatRepository
import empire.digiprem.chirp.infra.message_queue.EventPublisher
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher
) {


    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId?=null
    ): ChatMessage {
        val chat=chatRepository.findChatById(chatId,senderId)
            ?:throw ChatNotFoundException()

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?:throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id=messageId,
                content=content.trim(),
                chatId=chatId,
                chat=chat,
                sender = sender
            )

        )
        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId=sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId=chatId,
                message=savedMessage.content
            )
        )
        return savedMessage.toChatMessage()
    }


    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#result.chatId",
    )
    fun  deleteMessage(
        messageId: ChatMessageId,
        requestUserId:UserId,
    ) {
        val message=chatMessageRepository.findByIdOrNull(messageId)
            ?:throw MessageNotFoundException(messageId)

        if (message.sender.userId!=requestUserId){
            throw ForbiddenException()
        }
        chatMessageRepository.delete(message)


        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = message.id!!
            )
        )
        evictMessagesCache(chatId = message.chatId)


    }

    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun evictMessagesCache(chatId: ChatId) {

    }
}



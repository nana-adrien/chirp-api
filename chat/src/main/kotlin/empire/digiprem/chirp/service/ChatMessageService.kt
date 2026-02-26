package empire.digiprem.chirp.service

import empire.digiprem.chirp.api.dto.ChatMessageDto
import empire.digiprem.chirp.api.mappers.toChatMessageDto
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
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {


    @Transactional
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?=null,
        pageSize:Int,
    ):List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId=chatId,
                before=before?:Instant.now(),
                pegeable= PageRequest.of(0,pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

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

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                id=messageId,
                content=content.trim(),
                chatId=chatId,
                chat=chat,
                sender = sender
            )

        )
        return savedMessage.toChatMessage()
    }


    @Transactional
    fun  deleteMessage(
        messageId: ChatMessageId,
        requestUserId:UserId,
    ){
        val message=chatMessageRepository.findByIdOrNull(messageId)
            ?:throw MessageNotFoundException(messageId)

        if (message.sender.userId!=requestUserId){
            throw ForbiddenException()
        }
        chatMessageRepository.delete(message)
    }
}



package empire.digiprem.chirp.service

import empire.digiprem.chirp.api.dto.ChatMessageDto
import empire.digiprem.chirp.api.mappers.toChatMessageDto
import empire.digiprem.chirp.domain.event.ChatParticipantLeftEvent
import empire.digiprem.chirp.domain.exception.ChatNotFoundException
import empire.digiprem.chirp.domain.exception.ChatParticipantNotFoundException
import empire.digiprem.chirp.domain.exception.InvalidChatSizeException
import empire.digiprem.chirp.domain.models.Chat
import empire.digiprem.chirp.domain.models.ChatMessage
import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.exception.ForbiddenException
import empire.digiprem.chirp.infra.database.entities.ChatEntity
import empire.digiprem.chirp.infra.database.mappers.toChat
import empire.digiprem.chirp.infra.database.mappers.toChatMessage
import empire.digiprem.chirp.infra.database.repositories.ChatMessageRepository
import empire.digiprem.chirp.infra.database.repositories.ChatParticipantRepository
import empire.digiprem.chirp.infra.database.repositories.ChatRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {


    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before==null && #pageSize<=50",
        sync = true
    )
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



    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
    ): Chat {

        val otherParticipants = chatParticipantRepository
            .findByUserIdIn(otherUserIds)

        val allParticipants = (otherParticipants + creatorId)

        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat()
    }

    @Transactional
    fun addParticipantToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>

    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any { it.userId == requestUserId }

        if (!isRequestingUserInChat) {
            throw ForbiddenException()
        }


        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(chatId)
                ?: throw ChatParticipantNotFoundException(userId)
        }
        val lastMessage = lastMessage(chatId)
        val updateChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants.plus(users)
            }
        ).toChat(lastMessage)


        return updateChat
    }


    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantSize = chat.participants.size - 1
        if (newParticipantSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants-participant
            }
        )

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    private fun lastMessage(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatId(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }

}

package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.exception.ChatParticipantNotFoundException
import empire.digiprem.chirp.domain.exception.InvalidChatSizeException
import empire.digiprem.chirp.domain.models.Chat
import empire.digiprem.chirp.domain.models.ChatMessage
import empire.digiprem.chirp.domain.models.ChatParticipant
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.infra.database.entities.ChatParticipantEntity
import empire.digiprem.chirp.infra.database.mappers.toChat
import empire.digiprem.chirp.infra.database.repositories.ChatParticipantRepository
import empire.digiprem.chirp.infra.database.repositories.ChatRepository
import empire.digiprem.empire.digiprem.chirp.infra.database.entities.ChatEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
){

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
    ): Chat {

        val otherParticipants=chatParticipantRepository
            .findByUserIdIn(otherUserIds)

        val allParticipants=(otherParticipants + creatorId)

        if(allParticipants.size<2){
            throw InvalidChatSizeException()
        }

        val creator=chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator)+otherParticipants
            )
        ).toChat()
    }
}

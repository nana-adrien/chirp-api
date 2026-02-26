package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.models.ChatParticipant
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.infra.database.mappers.toChatParticipant
import empire.digiprem.chirp.infra.database.mappers.toChatParticipantEntity
import empire.digiprem.chirp.infra.database.repositories.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChatParticipant(
        chatParticipant: ChatParticipant,
    ){
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }


    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantsByEmailOrUsername(query:String): ChatParticipant? {
        val normalizedQuery = query.lowercase().trim()

        return  chatParticipantRepository.findByEmailOrUsername(
            normalizedQuery,
        )?.toChatParticipant()

    }
}
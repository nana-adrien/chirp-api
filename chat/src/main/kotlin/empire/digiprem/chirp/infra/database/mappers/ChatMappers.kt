package empire.digiprem.chirp.infra.database.mappers

import empire.digiprem.chirp.domain.models.Chat
import empire.digiprem.chirp.domain.models.ChatMessage
import empire.digiprem.chirp.domain.models.ChatParticipant
import empire.digiprem.chirp.infra.database.entities.ChatParticipantEntity
import empire.digiprem.empire.digiprem.chirp.infra.database.entities.ChatEntity


fun ChatEntity.toChat(lastMessage: ChatMessage?=null): Chat {
    return Chat(
        id = id!!,
        participants=participants.map { participant ->
            participant.toParticipant()
        }.toSet(),
        creator = creator.toParticipant(),
        lastActivityAt =  lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
        lastMessage = lastMessage
    )
}

fun ChatParticipantEntity.toParticipant(): ChatParticipant {
    return ChatParticipant(
        user = userId,
        userName = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}
package empire.digiprem.chirp.infra.database.mappers

import empire.digiprem.chirp.domain.models.Chat
import empire.digiprem.chirp.domain.models.ChatMessage
import empire.digiprem.chirp.domain.models.ChatParticipant
import empire.digiprem.chirp.infra.database.entities.ChatEntity
import empire.digiprem.chirp.infra.database.entities.ChatMessageEntity
import empire.digiprem.chirp.infra.database.entities.ChatParticipantEntity


fun ChatEntity.toChat(lastMessage: ChatMessage?=null): Chat {
    return Chat(
        id = id!!,
        participants=participants.map { participant ->
            participant.toChatParticipant()
        }.toSet(),
        creator = creator.toChatParticipant(),
        lastActivityAt =  lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
        lastMessage = lastMessage
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}


fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}


fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id!!,
        chatId = chatId,
        sender = sender.toChatParticipant(),
        content = content,
        createdAt = createdAt,
    )
}
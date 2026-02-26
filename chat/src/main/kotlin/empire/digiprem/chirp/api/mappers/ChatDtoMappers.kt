package empire.digiprem.chirp.api.mappers

import empire.digiprem.chirp.api.dto.ChatDto
import empire.digiprem.chirp.api.dto.ChatMessageDto
import empire.digiprem.chirp.api.dto.ChatParticipantDto
import empire.digiprem.chirp.domain.models.Chat
import empire.digiprem.chirp.domain.models.ChatMessage
import empire.digiprem.chirp.domain.models.ChatParticipant


fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map { it.toChatParticipantDto() },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        createdAt = createdAt
    )

}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId=chatId,
        senderId = sender.userId,
        content = content,
        createdAt = createdAt,
    )
}


fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )

}
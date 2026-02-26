package empire.digiprem.chirp.api.dto

import empire.digiprem.chirp.domain.type.ChatId
import java.time.Instant

data class ChatDto(
    val id: ChatId,
    val participants: List<ChatParticipantDto>,
    val lastActivityAt: Instant,
    val lastMessage:ChatMessageDto?,
    val createdAt: Instant
)

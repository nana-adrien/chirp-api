package empire.digiprem.chirp.api.dto

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId
import empire.digiprem.chirp.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val senderId: UserId,
    val createdAt: Instant,
)

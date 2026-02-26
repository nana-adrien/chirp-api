package empire.digiprem.chirp.domain.event

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)

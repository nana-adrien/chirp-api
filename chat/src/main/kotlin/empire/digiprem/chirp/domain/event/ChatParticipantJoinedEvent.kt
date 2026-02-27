package empire.digiprem.chirp.domain.event

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.UserId

data class ChatParticipantJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)

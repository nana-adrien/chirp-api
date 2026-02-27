package empire.digiprem.chirp.api.dto.ws

import empire.digiprem.chirp.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
)
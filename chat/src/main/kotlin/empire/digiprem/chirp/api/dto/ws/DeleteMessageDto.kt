package empire.digiprem.chirp.api.dto.ws

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId

data class DeleteMessageDto (
    val chatId: ChatId,
    val messageId: ChatMessageId
)
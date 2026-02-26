package empire.digiprem.chirp.domain.exception

import empire.digiprem.chirp.domain.type.ChatMessageId

class MessageNotFoundException(messageId: ChatMessageId) : RuntimeException(
    "Message with ID $messageId not found"
)
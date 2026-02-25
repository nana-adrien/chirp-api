package empire.digiprem.chirp.domain.models

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId
import org.apache.catalina.User
import java.time.Instant

class ChatMessage(
    val id:ChatMessageId,
    val chatId: ChatId,
    val sender: User,
    val content: String,
    val createdAt: Instant,

    )
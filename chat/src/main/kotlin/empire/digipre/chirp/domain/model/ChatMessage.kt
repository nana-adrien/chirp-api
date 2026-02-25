package empire.digipre.chirp.domain.model

import empire.digiprem.chirp.domain.type.ChatId
import empire.digiprem.chirp.domain.type.ChatMessageId
import org.apache.catalina.User
import java.time.Instant
import java.util.UUID

class ChatMessage(
    val id:ChatMessageId,
    val chatId: ChatId,
    val sender: User,
    val content: String,
    val createdAt: Instant,

    )
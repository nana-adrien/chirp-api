package empire.digiprem.chirp.api.dto.ws

enum class IncomingWebSocketMessageType {
    NEW_MESSAGE,
}

enum class OutgoingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATE,
    CHAT_PARTICIPANT_CHANGED,
    ERROR
}

data class IncomingWebSocketMessage(
    val type: IncomingWebSocketMessageType,
    val payload: String,
)

data class OutgoingWebSocketMessage(
    val type: OutgoingWebSocketMessageType,
    val payload: String,
)
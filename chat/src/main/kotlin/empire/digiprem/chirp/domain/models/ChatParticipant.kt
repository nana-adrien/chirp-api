package empire.digiprem.chirp.domain.models

import empire.digiprem.chirp.domain.type.UserId

data class ChatParticipant(
    val user: UserId,
    val userName: String,
    val email: String,
    val profilePictureUrl: String?,
)

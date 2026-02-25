package empire.digipre.chirp.domain.model

import empire.digiprem.chirp.domain.type.UserId
import org.apache.catalina.User

data class ChatParticipant(
    val user: UserId,
    val userName: String,
    val email: String,
    val profilePictureUrl: String?,
)

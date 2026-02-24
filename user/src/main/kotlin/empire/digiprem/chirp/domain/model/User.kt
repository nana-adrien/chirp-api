package empire.digiprem.chirp.domain.model

import empire.digiprem.empire.digiprem.chirp.domain.type.UserId


data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasVerifiedEmail: Boolean,
)

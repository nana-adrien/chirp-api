package empire.digiprem.chirp.api.dto

import empire.digiprem.chirp.domain.model.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean
)

package empire.digiprem.chirp.api.dto

import empire.digiprem.chirp.domain.model.AuthenticatedUser
import empire.digiprem.chirp.domain.model.User

data class AuthenticatedUserDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

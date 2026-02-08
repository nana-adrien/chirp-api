package empire.digiprem.chirp.api.dto.mappers

import empire.digiprem.chirp.api.dto.AuthenticatedUserDto
import empire.digiprem.chirp.api.dto.UserDto
import empire.digiprem.chirp.domain.model.AuthenticatedUser
import empire.digiprem.chirp.domain.model.User


fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken=accessToken,
        refreshToken=refreshToken,
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id =id,
        email =email,
        username =username,
        hasVerifiedEmail =hasVerifiedEmail
    )
}
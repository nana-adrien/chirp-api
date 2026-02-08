package empire.digiprem.chirp.infra.database.mappers

import empire.digiprem.chirp.domain.model.User
import empire.digiprem.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id=id!!,
        username=username!!,
        email=email!!,
        hasVerifiedEmail=hasVerifiedEmail,
    )
}
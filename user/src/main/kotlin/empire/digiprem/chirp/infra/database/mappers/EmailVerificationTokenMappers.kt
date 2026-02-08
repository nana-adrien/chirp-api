package empire.digiprem.chirp.infra.database.mappers

import empire.digiprem.chirp.domain.model.EmailVerificationToken
import empire.digiprem.chirp.infra.database.entities.EmailVerificationTokenEntity

class EmailVerificationTokenMappers {
}

fun EmailVerificationTokenEntity.toEmailVerificationToken() : EmailVerificationToken {
    return EmailVerificationToken(
       id = this.id,
        user=user.toUser(),
        token=token
    )
}
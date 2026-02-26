package empire.digiprem.chirp.api.util

import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.empire.digiprem.chirp.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?:throw UnauthorizedException()
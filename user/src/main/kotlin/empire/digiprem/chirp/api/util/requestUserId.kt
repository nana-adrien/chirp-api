package empire.digiprem.chirp.api.util

import empire.digiprem.chirp.domain.exception.UnauthorizedException
import empire.digiprem.chirp.domain.model.UserId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.client.HttpClientErrorException

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?:throw UnauthorizedException()
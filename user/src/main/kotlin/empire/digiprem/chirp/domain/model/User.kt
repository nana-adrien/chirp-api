package empire.digiprem.empire.digiprem.user.domain.model

import java.util.UUID
import javax.print.attribute.standard.RequestingUserName

typealias UserId = UUID

data class User(
    val id: UserId,
    val userName: String,
    val email: String,
    val hasEmailVerified: Boolean,
)

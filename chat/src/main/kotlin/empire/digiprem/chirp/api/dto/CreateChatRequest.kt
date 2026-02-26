package empire.digiprem.chirp.api.dto

import empire.digiprem.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

class CreateChatRequest(
    @field:Size(
        min = 1,
        message = "Chat must have at least one other participant",
    )
    val otherUserIds: List<UserId>,
)
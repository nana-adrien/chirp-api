package empire.digiprem.chirp.api.dto.request

import jakarta.validation.constraints.Email

data class EmailRequest(
    @field:Email
    val email: String
)
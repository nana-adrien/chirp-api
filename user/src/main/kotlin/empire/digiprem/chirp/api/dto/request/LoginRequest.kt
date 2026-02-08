package empire.digiprem.chirp.api.dto.request

import jakarta.validation.constraints.Email

data class LoginRequest (
    @field:Email
    val email: String,
    val password: String
)
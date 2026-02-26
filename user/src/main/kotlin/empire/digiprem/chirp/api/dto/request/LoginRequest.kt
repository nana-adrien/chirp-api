package empire.digiprem.chirp.api.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern

data class LoginRequest (
    @field:Email
    val email: String,
    @field:Password
    val password: String
)
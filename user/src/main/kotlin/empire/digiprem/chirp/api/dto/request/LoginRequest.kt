package empire.digiprem.chirp.api.dto.request

import empire.digiprem.chirp.api.util.Password
import jakarta.validation.constraints.Email

data class LoginRequest (
    @field:Email
    val email: String,
    @field:Password
    val password: String
)
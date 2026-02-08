package empire.digiprem.chirp.api.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,
    @field:Pattern(
        regexp = "^(?=.*[\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(.{8,})$",
        message = "Password must be at least 8 characters and contain at last one digit or special character"
    )
    val password: String
)

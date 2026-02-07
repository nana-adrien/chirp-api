package empire.digiprem.chirp.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,
    @field:Pattern(
        regexp = "",
        message = "Password must be at least 3 characters long"
    )
    val password: String
)

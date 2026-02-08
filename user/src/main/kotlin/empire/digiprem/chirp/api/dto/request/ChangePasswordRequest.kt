package empire.digiprem.chirp.api.dto.request

data class ChangePasswordRequest (
    val oldPassword: String,
    val newPassword: String
)
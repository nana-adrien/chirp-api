package empire.digiprem.chirp.domain.model

data class EmailVerificationToken (
    val id:Long,
    val user: User,
    val token: String,
)
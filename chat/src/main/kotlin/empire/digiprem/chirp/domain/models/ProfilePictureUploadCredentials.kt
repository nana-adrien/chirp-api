package empire.digiprem.chirp.domain.models

import java.time.Instant

data class ProfilePictureUploadCredentials(
    val uploadUrl: String,
    val publishUrl: String,
    val headers:Map<String, String>,
    val expiresAt: Instant

)
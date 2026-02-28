package empire.digiprem.chirp.api.dto

import empire.digiprem.chirp.domain.models.ProfilePictureUploadCredentials
import java.time.Instant

data class PictureUploadResponse(
    val uploadUrl: String,
    val publishUrl: String,
    val headers:Map<String, String>,
    val expiresAt: Instant
)

fun ProfilePictureUploadCredentials.toResponse():PictureUploadResponse {
    return PictureUploadResponse(
        uploadUrl=uploadUrl,
        publishUrl=publishUrl,
        headers=headers,
        expiresAt=expiresAt
    )
}
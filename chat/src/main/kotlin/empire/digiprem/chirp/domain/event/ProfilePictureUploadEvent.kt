package empire.digiprem.chirp.domain.event

import empire.digiprem.chirp.domain.type.UserId

data class ProfilePictureUploadEvent(
    val userId: UserId,
    val  newUrl: String?,
)
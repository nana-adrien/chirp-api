package empire.digiprem.chirp.api.dto.ws

import empire.digiprem.chirp.domain.type.UserId

data class ProfilePictureUploadDto(
    val userId: UserId,
    val newUrl: String?,
)
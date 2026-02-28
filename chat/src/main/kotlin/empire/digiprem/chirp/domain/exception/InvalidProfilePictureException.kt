package empire.digiprem.chirp.domain.exception

class InvalidProfilePictureException(
    override val message: String?=null,
):RuntimeException(message?:"Invalid Profile picture data")
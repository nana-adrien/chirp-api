package empire.digiprem.chirp.domain.exception

class StorageException(
    override val message: String?=null,
): RuntimeException(
    message ?:"unable to store file"
)
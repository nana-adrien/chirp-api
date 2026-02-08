package empire.digiprem.chirp.domain.exception

class UserAlreadyExistsException: Exception(
    "A user with this username or email already exists."
)
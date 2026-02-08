package empire.digiprem.chirp.exception

class UserAlreadyExistsException: Exception(
    "A user with this username or email already exists."
)
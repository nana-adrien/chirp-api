package empire.digiprem.chirp.domain.exception

import empire.digiprem.chirp.domain.type.UserId

class ChatParticipantNotFoundException(
  private val participantId: UserId
) : RuntimeException(
    "The chat participant with  the ID $participantId was not found."
)
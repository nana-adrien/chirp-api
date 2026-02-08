package empire.digiprem.chirp.domain.exception

class SamePasswordException: RuntimeException("The new password can't be equal to old one ") {
}
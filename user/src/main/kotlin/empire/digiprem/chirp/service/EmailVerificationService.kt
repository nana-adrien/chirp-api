package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.exception.InvalidTokenException
import empire.digiprem.chirp.domain.exception.UserNotFoundException
import empire.digiprem.chirp.domain.model.EmailVerificationToken
import empire.digiprem.chirp.infra.database.entities.EmailVerificationTokenEntity
import empire.digiprem.chirp.infra.database.mappers.toEmailVerificationToken
import empire.digiprem.chirp.infra.database.repositories.EmailVerificationTokenRepository
import empire.digiprem.chirp.infra.database.repositories.UserRepository
import empire.digiprem.empire.digiprem.chirp.domain.events.user.UserEvent
import empire.digiprem.empire.digiprem.chirp.infra.message_queue.EventPublisher
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher,
) {

    @Transactional
    fun createVerificationToken(email: String) : EmailVerificationToken {
        val userEntity=userRepository.findByEmail(email)?:throw UserNotFoundException()
        emailVerificationTokenRepository.invalidateActiveTokensForUser(
            userEntity
        )
        val token= EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user=userEntity
        )
        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun resendVerificationEmail(email:String){
        val token=createVerificationToken(email)
        if (token.user.hasVerifiedEmail){
            return
        }

        eventPublisher.publish(
            UserEvent.RequestResendVerification(
                userId = token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )

    }
    @Transactional
    fun verifyEmail(token: String){
        val verificationToken=emailVerificationTokenRepository.findByToken(token)
            ?:throw InvalidTokenException("Email verification token is invalid")

        if (verificationToken.isUsed){
            throw InvalidTokenException("Email verification token is already used")
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException("Expired verification token has already expired")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt=Instant.now()
            }
        )
       userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail=true
            }
        )

    }
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens(){
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}
package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.exception.InvalidTokenException
import empire.digiprem.chirp.domain.exception.UserNotFoundException
import empire.digiprem.chirp.domain.model.EmailVerificationToken
import empire.digiprem.chirp.infra.database.entities.EmailVerificationTokenEntity
import empire.digiprem.chirp.infra.database.mappers.toEmailVerificationToken
import empire.digiprem.chirp.infra.database.repositories.EmailVerificationTokenRepository
import empire.digiprem.chirp.infra.database.repositories.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long
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

    fun resendVerificationEmail(email:String){


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
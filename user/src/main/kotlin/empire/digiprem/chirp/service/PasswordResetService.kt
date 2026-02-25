package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.exception.InvalidCredentialsException
import empire.digiprem.chirp.domain.exception.InvalidTokenException
import empire.digiprem.chirp.domain.exception.SamePasswordException
import empire.digiprem.chirp.domain.exception.UserNotFoundException
import empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.infra.database.entities.PasswordResetTokenEntity
import empire.digiprem.chirp.infra.database.repositories.PasswordResetTokenRepository
import empire.digiprem.chirp.infra.database.repositories.RefreshTokenRepository
import empire.digiprem.chirp.infra.database.repositories.UserRepository
import empire.digiprem.chirp.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val passwordResetRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}")
    private val expiryminutes: Long,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository

) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryminutes, ChronoUnit.MINUTES)
        )
        passwordResetRepository.save(token)

        //TODO: Inform notification service about password reset trigger to send email

    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetRepository.findByToken(token)
            ?: throw InvalidTokenException("invalid Password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Expired verification token has already expired")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }
        val hashedNewPassword = passwordEncoder.encode(newPassword)!!

        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )

        passwordResetRepository.save(
            resetToken.apply {
                this.usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }


    fun changePassword(userId: UserId, oldPassword:String, newPassword: String){
        val user=userRepository.findByIdOrNull(userId)?:throw UserNotFoundException()

        if (passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }
        if (oldPassword==newPassword){
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)
        val newHashedPassword = passwordEncoder.encode(newPassword)!!
        userRepository.save(
            user.apply {
                this.hashedPassword=newHashedPassword
            }
        )


    }

    @Scheduled(cron="0 0 3 * * *" )
    fun cleanupExpiredTokens(){
        passwordResetRepository.deleteByExpiresAtLessThan(
            now= Instant.now()
        )
    }


}
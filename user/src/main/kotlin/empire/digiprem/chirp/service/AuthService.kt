package empire.digiprem.chirp.service

import empire.digiprem.chirp.domain.exception.EmailNotVerifiedException
import empire.digiprem.chirp.domain.model.AuthenticatedUser
import empire.digiprem.chirp.domain.model.User
import empire.digiprem.empire.digiprem.chirp.domain.type.UserId
import empire.digiprem.chirp.domain.exception.InvalidCredentialsException
import empire.digiprem.chirp.domain.exception.InvalidTokenException
import empire.digiprem.chirp.domain.exception.UserAlreadyExistsException
import empire.digiprem.chirp.domain.exception.UserNotFoundException
import empire.digiprem.chirp.infra.database.entities.RefreshTokenEntity
import empire.digiprem.chirp.infra.database.entities.UserEntity
import empire.digiprem.chirp.infra.database.mappers.toUser
import empire.digiprem.chirp.infra.database.repositories.RefreshTokenRepository
import empire.digiprem.chirp.infra.database.repositories.UserRepository
import empire.digiprem.chirp.infra.security.PasswordEncoder
import empire.digiprem.empire.digiprem.chirp.domain.events.user.UserEvent
import empire.digiprem.empire.digiprem.chirp.infra.message_queue.EventPublisher
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jWTService: JWTService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher,
) {


    @Transactional
    fun register(email:String, username: String, password: String): User {
        val trimEmail=email.trim()
        val user = userRepository.findByEmailOrUsername(trimEmail,username.trim())
        if (user!=null){
            throw UserAlreadyExistsException()
        }

        val savedUser=userRepository.save(
            UserEntity(
                email=trimEmail,
                username=username,
                hashedPassword = passwordEncoder.encode(password)!!
            )
        ).toUser()

       val token= emailVerificationService.createVerificationToken(trimEmail)
        eventPublisher.publish(
            UserEvent.Created(
                userId = savedUser.id,
                email = savedUser.email,
                username = savedUser.username,
                verificationToken = token.token
            )
        )
        return savedUser
    }

    fun login(email:String,password: String): AuthenticatedUser {
        val user=userRepository.findByEmail(email.trim())?:throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password,user.hashedPassword)){
            throw InvalidCredentialsException()
        }

        if (!user.hasVerifiedEmail){
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userId->
            val accessToken=jWTService.generateAccessToken(userId)
            val refreshToken=jWTService.generateRefreshToken(userId)

            storeRefreshToken(userId,refreshToken)

            AuthenticatedUser(
                user=user.toUser(),
                accessToken=accessToken,
                refreshToken=refreshToken
            )
        }?:throw UserNotFoundException()


    }

    @Transactional
    fun refresh(refreshToken:String): AuthenticatedUser{
        if (!jWTService.validateRefreshToken(refreshToken)){
            throw InvalidTokenException(
                message = "Invalid refresh token",
            )
        }

        val userId=jWTService.getUserIdFromToken(refreshToken)
        val user=userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        val hashed=hashedToken(refreshToken)

        return user.id?.let { userId->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId=userId,
                hashedToken=hashed)?:throw InvalidTokenException(message = "Invalid refresh token",)

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId=userId,
                hashedToken=hashed
            )
            val newAccessToken=jWTService.generateAccessToken(userId)
            val newRefreshToken=jWTService.generateRefreshToken(userId)

            storeRefreshToken(userId,newRefreshToken)

            AuthenticatedUser(
                user=user.toUser(),
                accessToken = newAccessToken,
                refreshToken=newRefreshToken
            )

        }?:throw UserNotFoundException()

    }

    @Transactional
    fun logout(refreshToken: String){
        val userId= jWTService.getUserIdFromToken(refreshToken)
        val hashed=hashedToken(refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId,hashed)
    }

    private fun storeRefreshToken(userId: UserId, token: String){
        val hashed=hashedToken(token)
        val expiryMs=jWTService.refreshTokenValidityMs
        val expiresAt= Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId=userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashedToken(token: String):String{
        val digest= MessageDigest.getInstance("SHA-256")
        val hashBytes=digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
package empire.digiprem.chirp.service.auth

import empire.digiprem.chirp.domain.model.AuthenticatedUser
import empire.digiprem.chirp.domain.model.User
import empire.digiprem.chirp.domain.model.UserId
import empire.digiprem.chirp.exception.InvalidCredentialsException
import empire.digiprem.chirp.exception.InvalidTokenException
import empire.digiprem.chirp.exception.UserAlreadyExistsException
import empire.digiprem.chirp.exception.UserNotFoundException
import empire.digiprem.chirp.infra.database.entities.RefreshTokenEntity
import empire.digiprem.chirp.infra.database.entities.UserEntity
import empire.digiprem.chirp.infra.database.mappers.toUser
import empire.digiprem.chirp.infra.database.repositories.RefreshTokenRepository
import empire.digiprem.chirp.infra.database.repositories.UserRepository
import empire.digiprem.chirp.infra.security.PasswordEncoder
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
    private val refreshTokenRepository: RefreshTokenRepository
) {


    fun register(email:String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(email.trim(),username.trim())
        if (user!=null){
            throw UserAlreadyExistsException()
        }

        val savedUser=userRepository.save(
            UserEntity(
                email=email,
                username=username,
                hashedPassword = passwordEncoder.encode(password)!!
            )
        ).toUser()

        return savedUser
    }


    fun login(email:String,password: String): AuthenticatedUser {
        val user=userRepository.findByEmail(email.trim())?:throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password,user.hashedPassword)){
            throw InvalidCredentialsException()
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
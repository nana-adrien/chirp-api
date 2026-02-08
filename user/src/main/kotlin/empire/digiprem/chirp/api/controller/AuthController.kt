package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.dto.AuthenticatedUserDto
import empire.digiprem.chirp.api.dto.UserDto
import empire.digiprem.chirp.api.dto.mappers.toAuthenticatedUserDto
import empire.digiprem.chirp.api.dto.mappers.toUserDto
import empire.digiprem.chirp.api.dto.request.LoginRequest
import empire.digiprem.chirp.api.dto.request.RefreshTokenRequest
import empire.digiprem.chirp.api.dto.request.RegisterRequest
import empire.digiprem.chirp.infra.database.repositories.EmailVerificationTokenRepository
import empire.digiprem.chirp.service.auth.AuthService
import empire.digiprem.chirp.service.auth.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val emailVerificationService: EmailVerificationService
) {

    @PostMapping("/register")
    fun register(
      @Valid @RequestBody  body: RegisterRequest
    ): UserDto{
       return authService.register(body.email, body.username, body.password).toUserDto()
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body : LoginRequest
    ): AuthenticatedUserDto{
        return authService.login(body.email,body.password).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody body: RefreshTokenRequest
    ):AuthenticatedUserDto{
        return authService.refresh(body.refreshToken).toAuthenticatedUserDto()
    }
    @PostMapping("/logout")
    fun logout(
        @RequestBody body: RefreshTokenRequest
    ){
         authService.logout(body.refreshToken)
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ){
        emailVerificationService.verifyEmail(token)
    }

}
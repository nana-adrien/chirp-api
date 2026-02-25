package empire.digiprem.chirp.api.controller

import empire.digiprem.chirp.api.dto.AuthenticatedUserDto
import empire.digiprem.chirp.api.dto.UserDto
import empire.digiprem.chirp.api.dto.mappers.toAuthenticatedUserDto
import empire.digiprem.chirp.api.dto.mappers.toUserDto
import empire.digiprem.chirp.api.dto.request.ChangePasswordRequest
import empire.digiprem.chirp.api.dto.request.EmailRequest
import empire.digiprem.chirp.api.dto.request.LoginRequest
import empire.digiprem.chirp.api.dto.request.RefreshTokenRequest
import empire.digiprem.chirp.api.dto.request.RegisterRequest
import empire.digiprem.chirp.api.dto.request.ResetPasswordRequest
import empire.digiprem.chirp.api.util.requestUserId
import empire.digiprem.chirp.infra.config.IpRateLimit
import empire.digiprem.chirp.infra.rate_limiting.EmailRateLimiter
import empire.digiprem.chirp.infra.rate_limiting.IpRateLimiter
import empire.digiprem.chirp.service.AuthService
import empire.digiprem.chirp.service.EmailVerificationService
import empire.digiprem.chirp.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter,
    private val ipRateLimiter: IpRateLimiter,
) {
    @PostMapping("/register")
    @IpRateLimit(
        request = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(body.email, body.username, body.password).toUserDto()
    }

    @PostMapping("/login")
    @IpRateLimit(
        request = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(body.email, body.password).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody body: RefreshTokenRequest
    ): AuthenticatedUserDto {
        return authService.refresh(body.refreshToken).toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody body: RefreshTokenRequest
    ) {
        authService.logout(body.refreshToken)
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/resend-verification")
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest
    ) {
        emailRateLimiter.withRateLimit(
            email = body.email
        ) {
            emailVerificationService.resendVerificationEmail(body.email)
        }
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest,
    ) {
        passwordResetService.changePassword(
            userId = requestUserId ,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword
        )
    }


}
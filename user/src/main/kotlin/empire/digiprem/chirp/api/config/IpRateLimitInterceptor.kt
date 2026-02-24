package empire.digiprem.chirp.api.config

import empire.digiprem.chirp.domain.exception.RateLimitException
import empire.digiprem.chirp.infra.config.IpRateLimit
import empire.digiprem.chirp.infra.rate_limiting.IpRateLimiter
import empire.digiprem.chirp.infra.rate_limiting.IpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitInterceptor (
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${chirp.rate-limiting.ip.apply-limit}")
    private val applyLimit:Boolean,
): HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if(handler is HandlerMethod && applyLimit) {
            val annotation=handler.getMethodAnnotation(IpRateLimit::class.java)
            if (annotation != null) {
                val clientIo=ipResolver.getClientIp(request)

                return  try{
                    return ipRateLimiter.withRateLimit(
                        ipAddress = clientIo,
                        resetsIn = Duration.of(
                            annotation.duration,
                            annotation.unit.toChronoUnit()
                        ),
                        maxRequestsPerIp = annotation.request,
                        action ={true}
                    )
                }catch (e: RateLimitException){
                    response.sendError(429)
                    false
                }
            }
        }


     return  super.preHandle(request, response, handler)
    }
}
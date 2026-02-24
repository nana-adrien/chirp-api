package empire.digiprem.chirp.infra.rate_limiting

import empire.digiprem.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IpRateLimiter(
    private val redisTemplate: StringRedisTemplate,
) {

    companion object {
        private const val IP_RATE_LIMIT_PREFIX = "rate_limit:ip"
        private const val IP_ATTEMPT_COUNT_PREFIX = "ip_attempt_count:"
    }

    @Value("classpath:ip_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use { it.readBytes().decodeToString() }
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }
    fun <T>withRateLimit(
        ipAddress: String,
        resetsIn: Duration,
        maxRequestsPerIp:Int,
        action:()-> T
    ):T {
        val key = "$IP_RATE_LIMIT_PREFIX:$ipAddress"

        val result=redisTemplate.execute(
            rateLimitScript,
            listOf(key, ),
            maxRequestsPerIp.toString(),
            resetsIn.seconds.toString(),
        )
        val currentCount=result[0]

        return  if (currentCount<=maxRequestsPerIp) {
            action()
        }else{
            val ttl=result[1]
            throw RateLimitException(resetInSeconds = ttl)
        }
    }
}
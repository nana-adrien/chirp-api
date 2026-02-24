package empire.digiprem.chirp.infra.rate_limiting

import empire.digiprem.chirp.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address
import kotlin.math.log

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {

    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map {
            IpAddressMatcher(it)
        }

        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )


    }

    private val logger: Logger = LoggerFactory.getLogger(IpResolver::class.java)
    private val trustedMatcher: List<IpAddressMatcher> = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy
                proxy.contains(":") -> "$proxy/128"
                else -> "$proxy/32"
            }
            IpAddressMatcher(cidr)
        }


    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr

        if (!isFromTrustedProxy(remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connexion attempt from $remoteAddr")
                throw SecurityException("No valid client IP in proxy headers")
            }

            return remoteAddr
        }

        val clientIp = extractFromXRealIp(request, remoteAddr)
        if (clientIp != null) {
            logger.warn("No Valid client ip in proxy hearders")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy headers")
            }
        }
        return  clientIp?: remoteAddr
    }

    private fun extractFromXRealIp(
        request: HttpServletRequest,
        proxyIp: String,
    ): String? {
        return request.getHeader("X-Real_IP")?.let { hearder ->
            validateAndNormalizeIp(
                ip = hearder,
                hearderName = "X-Real-IP",
                proxyIp = proxyIp
            )
        }
    }

    private fun validateAndNormalizeIp(ip: String, hearderName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()

        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.debug("Invalid IP in $hearderName header from proxy $proxyIp: $ip")
            return null
        }
        return try {
            val inetAddr = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> Inet4Address.getByName("$trimmedIp")
                else -> {
                    logger.warn("Invalid IP format in $hearderName:$trimmedIp from proxy $proxyIp")
                    return null
                }
            }
            if (isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in  $hearderName:$trimmedIp  header from proxy $proxyIp: $ip")
                null
            }
            inetAddr.hostAddress
        } catch (e: Exception) {
            logger.warn("Invalid IP format in $hearderName:$trimmedIp  header from proxy $proxyIp: $ip")
            null
        }
    }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip) }
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatcher.any { it.matches(ip) }
    }


}
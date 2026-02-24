package empire.digiprem.chirp.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@ConfigurationProperties(prefix = "nginx")
class NginxConfig(
    val trustedIps:List<String> = emptyList(),
    val requireProxy:Boolean = true,

) {
}
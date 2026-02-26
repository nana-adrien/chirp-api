package empire.digiprem.chirp.api.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import empire.digiprem.chirp.domain.ChirpEvent
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration
import java.time.Duration.ofMinutes

@Configuration
@EnableCaching
class Re0disConfig {


    @Bean
    fun cacheManager(
        connectionFactory: LettuceConnectionFactory,
    ): RedisCacheManager {
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            findAndRegisterModules()
            val polymorphicModule = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(ChirpEvent::class.java)
                .allowIfSubType("java.util.")
                .allowIfBaseType("kotlin.collections.")
                .allowIfSubType("empire.digiprem.chirp")
                .build()

            activateDefaultTyping(polymorphicModule, ObjectMapper.DefaultTyping.NON_FINAL)
        }
        val cacheConfig= RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(  Duration.ofHours(1L) )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(cacheConfig)
            .withCacheConfiguration(
                "messages",
                cacheConfig.entryTtl(Duration.ofMinutes(30))
            )
            .transactionAware()
            .build()


    }
}
package com.example.picsingularcore.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig {
    @Value("\${spring.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.redis.port}")
    private lateinit var redisPort: String

    @Value("\${spring.redis.connect-timeout}")
    private lateinit var redisTimeout: String

//    @Value("\${spring.redis.jedis.pool.max-active}")
//    private lateinit var redisMaxActive: String

    @Value("\${spring.redis.jedis.pool.max-idle}")
    private lateinit var redisMaxIdle: String

    @Value("\${spring.redis.jedis.pool.min-idle}")
    private lateinit var redisMinIdle: String

    @Value("\${spring.redis.jedis.pool.max-wait}")
    private lateinit var redisMaxWait: String

    // custom redis template, make it json serializable
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        val om = ObjectMapper()
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL)
        jackson2JsonRedisSerializer.setObjectMapper(om)
        val stringRedisSerializer = StringRedisSerializer()
        redisTemplate.keySerializer = stringRedisSerializer
        redisTemplate.valueSerializer = stringRedisSerializer
        redisTemplate.hashKeySerializer = stringRedisSerializer
        redisTemplate.hashValueSerializer = stringRedisSerializer
        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        // set host and port
        val standaloneConfiguration = RedisStandaloneConfiguration()
        standaloneConfiguration.hostName = redisHost
        standaloneConfiguration.port = redisPort.toInt()
        val poolConfig = GenericObjectPoolConfig<String>()
        poolConfig.maxIdle = redisMaxIdle.toInt()
        poolConfig.minIdle = redisMinIdle.toInt()
//        poolConfig.maxTotal = redisMaxActive.toInt()

        val lettuceClientConfiguration = LettucePoolingClientConfiguration .builder()
                .poolConfig(poolConfig)
                .commandTimeout(Duration.ofMillis(redisTimeout.toLong()))
                .build()

        return LettuceConnectionFactory(standaloneConfiguration, lettuceClientConfiguration)
    }
}
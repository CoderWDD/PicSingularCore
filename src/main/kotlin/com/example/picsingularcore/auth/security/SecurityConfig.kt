package com.example.picsingularcore.auth.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig () {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 对登录注册无条件放行
            .authorizeRequests()
            .antMatchers("/user/login", "/user/register").permitAll()
            .anyRequest().authenticated()
        // 加载自定义的过滤器
//            .withObjectPostProcessor(object : WebSecurityCustomizer {
//                override fun customize(web: HttpSecurity) {
//                    web.headers().frameOptions().disable()
//                }
//            })
        return http.build()
    }

}
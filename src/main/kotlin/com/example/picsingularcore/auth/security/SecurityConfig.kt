package com.example.picsingularcore.auth.security

import com.example.picsingularcore.auth.filter.JwtAccessDeniedHandler
import com.example.picsingularcore.auth.filter.JwtAuthenticationEntryPoint
import com.example.picsingularcore.auth.filter.JwtRequestFilter
import com.example.picsingularcore.common.constant.RolesConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig () {
    @Autowired
    lateinit var jwtRequestFilter: JwtRequestFilter

    @Autowired
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    @Autowired
    lateinit var jwtAccessDeniedHandler: JwtAccessDeniedHandler

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 对登录注册无条件放行
            .authorizeRequests()
            .antMatchers("/user/login", "/user/register").permitAll()
            .antMatchers("/admin/**").hasRole(RolesConstant.ADMIN.name)
            .antMatchers("/user/**").hasRole(RolesConstant.USER.name)
            .antMatchers("/supper_admin/**").hasRole(RolesConstant.SUPER_ADMIN.name)
            .anyRequest().authenticated()

        // add custom exception handler
        http
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

        // add custom filter
        http.addFilterBefore(jwtRequestFilter,UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
package com.example.picsingularcore.auth.filter

import com.example.picsingularcore.common.utils.JwtUtil
import com.example.picsingularcore.service.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtRequestFilter: OncePerRequestFilter() {

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @Autowired
    lateinit var userDetailsService: UserDetailsServiceImpl

    private lateinit var username: String

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // get the token from the request
        request.getHeader("Authorization")?.let {
            if (it.startsWith("Bearer ")) {
                request.setAttribute("jwt", it.substring(7))
            }
        }

        val jwtToken = request.getAttribute("jwt")
        // check if the token is valid
        if (jwtToken != null && jwtUtil.isTokenValid(jwtToken.toString())) {
            // if the token is valid, set the username in the request
            username = jwtUtil.getUsernameFromToken(jwtToken.toString())
            request.setAttribute("username", username)
        }else{
            // if the token is not valid, then do FilterChain.doFilter(), and the request will be handled by the spring security
            filterChain.doFilter(request, response)
            return
        }

        // bind the user details to authentication
        userDetailsService.loadUserByUsername(username).let {
            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(it, null, it.authorities)
            usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
        }

        filterChain.doFilter(request, response)
    }
}
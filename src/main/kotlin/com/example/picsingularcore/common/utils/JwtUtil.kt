package com.example.picsingularcore.common.utils

import com.example.picsingularcore.pojo.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*

@Component
class JwtUtil: Serializable {
    companion object {
        private const val serialVersionUID = -3301605555790776463L

        private const val SECRET_KEY = "CoderWdd"

        private const val EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000

        private val SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256

        private const val ISSued_BY = "Picsingular"

    }

    fun generateToken(map: Map<String,Any>,username: String): String{
        val nowMillis = System.currentTimeMillis()
        val expMillis = nowMillis + EXPIRATION_TIME
        // TODO bug fix: Unsigned Claims JWTs are not supported

        return Jwts
            .builder()
            .setClaims(map)
            .setSubject(username)
            .setIssuer(ISSued_BY)
            .setIssuedAt(Date(nowMillis))
            .signWith(SIGNATURE_ALGORITHM, SECRET_KEY)
            .setExpiration(Date(expMillis))
            .compact()
    }

    fun isTokenExpired(token: String): Boolean{
        val nowMillis = System.currentTimeMillis()
        val jwt = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token)
        val expMillis = jwt.body.expiration.time
        return expMillis < nowMillis
    }

    fun getUsernameFromToken(token: String): String{
        val jwt = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token)
        return jwt.body.subject
    }

    fun getClaimFromToken(token: String, claim: String): Any?{
        val jwt = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token)
        return jwt.body[claim]
    }

    fun getPasswordFromToken(token: String): String{
        // get password from claim
        return (getClaimFromToken(token, "user") as User).password
    }

    fun isTokenValid(token: String): Boolean{
        return !isTokenExpired(token)
    }
}
package com.example.picsingularcore.common.utils

import com.example.picsingularcore.pojo.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil: Serializable {
    companion object {
        private const val serialVersionUID = -3301605555790776463L

        private const val SECRET_KEY = "CoderWdd"

        private const val EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L

        private var SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512

        private const val ISSued_BY = "CoderWdd"

    }

    fun generateToken(map: Map<String,Any>,username: String): String{
        val nowMillis = System.currentTimeMillis()
        val expMillis = nowMillis + EXPIRATION_TIME

        return Jwts
            .builder()
            .setClaims(map)
            .setSubject(username)
            .setIssuer(ISSued_BY)
            .setIssuedAt(Date(nowMillis))
            .signWith(SIGNATURE_ALGORITHM, decodeSecretKey())
            .setExpiration(Date(expMillis))
            .compact()
    }

    fun isTokenExpired(token: String): Boolean{
        val nowMillis = System.currentTimeMillis()
        val expMillis = parseToken(token).expiration.time
        return expMillis < nowMillis
    }

    fun getUsernameFromToken(token: String): String{
        return parseToken(token).subject
    }


    fun getUserFromToken(token: String): User?{
        val claims = parseToken(token)
        val map = claims["user"] as Map<*, *>
        return User(
            userId = if (map["userId"] == null) null else map["userId"].toString().toLong(),
            username = if (map["username"] == null) "" else map["username"].toString(),
            password = if (map["password"] == null) "" else map["password"] as String,
            avatar = if (map["avatar"] == null) null else map["avatar"] as String,
            signature = if (map["signature"] == null) null else map["signature"] as String,
        )
    }

    fun getPasswordFromToken(token: String): String?{
        val user = getUserFromToken(token)
        return user?.password
    }

    fun isTokenValid(token: String): Boolean{
        return !isTokenExpired(token)
    }

    fun parseToken(token: String): Claims{
        return Jwts.parser().setSigningKey(decodeSecretKey()).parseClaimsJws(token).body
    }

    fun decodeSecretKey(): SecretKey{
        var decode = Base64.getDecoder().decode(SECRET_KEY)
        return SecretKeySpec(decode, 0, decode.size, "AES")
    }
}
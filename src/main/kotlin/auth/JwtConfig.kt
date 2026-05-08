package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.Date

class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTtlMinutes: Long,
    val refreshTtlDays: Long
) {
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun makeAccessToken(userId: Int, role: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("uid", userId)
            .withClaim("role", role)
            .withClaim("type", "access")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + accessTtlMinutes * 60_000))
            .sign(algorithm)
    }

    fun makeRefreshToken(userId: Int): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("uid", userId)
            .withClaim("type", "refresh")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + refreshTtlDays * 24L * 3600_000L))
            .sign(algorithm)
    }

    companion object {
        fun fromConfig(cfg: ApplicationConfig): JwtConfig {
            val sub = cfg.config("jwt")
            return JwtConfig(
                secret = sub.property("secret").getString(),
                issuer = sub.property("issuer").getString(),
                audience = sub.property("audience").getString(),
                realm = sub.property("realm").getString(),
                accessTtlMinutes = sub.property("accessTtlMinutes").getString().toLong(),
                refreshTtlDays = sub.property("refreshTtlDays").getString().toLong()
            )
        }
    }
}

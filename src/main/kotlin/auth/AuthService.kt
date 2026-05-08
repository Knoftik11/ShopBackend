package com.example.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.example.repos.UserRepository

class AuthService(
    private val users: UserRepository,
    private val jwt: JwtConfig
) {

    private val refreshVerifier: JWTVerifier = JWT.require(jwt.algorithm)
        .withIssuer(jwt.issuer)
        .withAudience(jwt.audience)
        .build()

    fun register(req: RegisterRequest): AuthResponse {
        val email = req.email.trim().lowercase()
        require(email.isNotEmpty() && "@" in email) { "некорректный email" }
        require(req.password.length >= 6) { "пароль слишком короткий" }
        require(req.name.isNotBlank()) { "имя пустое" }

        if (users.findByEmail(email) != null) {
            throw IllegalArgumentException("пользователь уже существует")
        }
        val hash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())
        val u = users.create(req.name.trim(), email, hash)
        return tokensFor(u.id, u.role).let {
            AuthResponse(it.first, it.second, u.toDto())
        }
    }

    fun login(req: LoginRequest): AuthResponse {
        val email = req.email.trim().lowercase()
        val u = users.findByEmail(email) ?: throw IllegalArgumentException("неверный email или пароль")
        val ok = BCrypt.verifyer().verify(req.password.toCharArray(), u.passwordHash).verified
        if (!ok) throw IllegalArgumentException("неверный email или пароль")
        val t = tokensFor(u.id, u.role)
        return AuthResponse(t.first, t.second, u.toDto())
    }

    fun refresh(refreshToken: String): AuthResponse {
        val decoded = try {
            refreshVerifier.verify(refreshToken)
        } catch (e: Exception) {
            throw IllegalArgumentException("invalid refresh token")
        }
        val type = decoded.getClaim("type").asString()
        if (type != "refresh") throw IllegalArgumentException("not a refresh token")
        val uid = decoded.getClaim("uid").asInt() ?: throw IllegalArgumentException("bad token")
        val u = users.findById(uid) ?: throw IllegalArgumentException("user not found")
        val t = tokensFor(u.id, u.role)
        return AuthResponse(t.first, t.second, u.toDto())
    }

    private fun tokensFor(userId: Int, role: String): Pair<String, String> =
        jwt.makeAccessToken(userId, role) to jwt.makeRefreshToken(userId)
}

package com.example

import com.auth0.jwt.JWT
import com.example.auth.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

lateinit var jwtConfig: JwtConfig
    private set

fun Application.configureSecurity() {
    jwtConfig = JwtConfig.fromConfig(environment.config)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(jwtConfig.algorithm)
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )
            validate { credential ->
                val type = credential.payload.getClaim("type").asString()
                if (type == "access" && credential.payload.getClaim("uid").asInt() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}

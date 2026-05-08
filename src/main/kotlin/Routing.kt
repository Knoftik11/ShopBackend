package com.example

import com.example.auth.AuthService
import com.example.repos.UserRepository
import com.example.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepo = UserRepository()
    val authService = AuthService(userRepo, jwtConfig)

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/ping") {
            call.respond(mapOf("status" to "ok"))
        }
        authRoutes(authService)
    }
}
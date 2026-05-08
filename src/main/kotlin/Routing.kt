package com.example

import com.example.auth.AuthService
import com.example.repos.ProductRepository
import com.example.repos.UserRepository
import com.example.routes.authRoutes
import com.example.routes.productRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepo = UserRepository()
    val productRepo = ProductRepository()
    val authService = AuthService(userRepo, jwtConfig)

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/ping") {
            call.respond(mapOf("status" to "ok"))
        }
        authRoutes(authService)
        productRoutes(productRepo)
    }
}
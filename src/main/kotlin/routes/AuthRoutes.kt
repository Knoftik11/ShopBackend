package com.example.routes

import com.example.auth.AuthService
import com.example.auth.LoginRequest
import com.example.auth.RefreshRequest
import com.example.auth.RegisterRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(service: AuthService) {
    route("/auth") {
        post("/register") {
            val body = call.receive<RegisterRequest>()
            val res = service.register(body)
            call.respond(HttpStatusCode.Created, res)
        }
        post("/login") {
            val body = call.receive<LoginRequest>()
            val res = service.login(body)
            call.respond(res)
        }
        post("/refresh") {
            val body = call.receive<RefreshRequest>()
            val res = service.refresh(body.refreshToken)
            call.respond(res)
        }
    }
}

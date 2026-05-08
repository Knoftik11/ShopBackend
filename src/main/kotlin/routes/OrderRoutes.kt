package com.example.routes

import com.example.orders.CreateOrderRequest
import com.example.orders.UpdateStatusRequest
import com.example.repos.OrderRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(repo: OrderRepository) {
    authenticate("auth-jwt") {
        route("/orders") {
            post {
                val uid = currentUser() ?: return@post
                val body = runCatching { call.receive<CreateOrderRequest>() }
                    .getOrDefault(CreateOrderRequest())
                val created = repo.createFromCart(uid.userId, body.address)
                call.respond(HttpStatusCode.Created, created)
            }
            get {
                val uid = currentUser() ?: return@get
                call.respond(repo.findByUser(uid.userId))
            }
            get("/{id}") {
                val u = currentUser() ?: return@get
                val orderId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("плохой id")
                val filter = if (u.role == "ADMIN") null else u.userId
                val order = repo.findById(orderId, filter)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(order)
            }
            patch("/{id}/status") {
                val u = currentUser() ?: return@patch
                if (u.role != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@patch
                }
                val orderId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("плохой id")
                val body = call.receive<UpdateStatusRequest>()
                if (repo.updateStatus(orderId, body.status)) {
                    val updated = repo.findById(orderId)
                    call.respond(updated ?: HttpStatusCode.NotFound)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private data class CurrentUser(val userId: Int, val role: String)

private suspend fun io.ktor.server.routing.RoutingContext.currentUser(): CurrentUser? {
    val payload = call.principal<JWTPrincipal>()?.payload
    val uid = payload?.getClaim("uid")?.asInt()
    val role = payload?.getClaim("role")?.asString() ?: "USER"
    if (uid == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return CurrentUser(uid, role)
}

package com.example.routes

import com.example.cart.AddToCartRequest
import com.example.repos.CartRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cartRoutes(repo: CartRepository) {
    authenticate("auth-jwt") {
        route("/cart") {
            get {
                val uid = currentUserId() ?: return@get
                call.respond(repo.getByUser(uid))
            }
            post {
                val uid = currentUserId() ?: return@post
                val body = call.receive<AddToCartRequest>()
                if (body.quantity <= 0) throw IllegalArgumentException("количество должно быть положительным")
                repo.addOrUpdate(uid, body.productId, body.quantity)
                call.respond(repo.getByUser(uid))
            }
            delete {
                val uid = currentUserId() ?: return@delete
                repo.clear(uid)
                call.respond(HttpStatusCode.NoContent)
            }
            delete("/{itemId}") {
                val uid = currentUserId() ?: return@delete
                val itemId = call.parameters["itemId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("плохой itemId")
                if (repo.removeItem(uid, itemId)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private suspend fun io.ktor.server.routing.RoutingContext.currentUserId(): Int? {
    val uid = call.principal<JWTPrincipal>()?.payload?.getClaim("uid")?.asInt()
    if (uid == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uid
}

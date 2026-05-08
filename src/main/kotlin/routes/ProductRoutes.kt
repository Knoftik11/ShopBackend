package com.example.routes

import com.example.products.PageResponse
import com.example.products.ProductCreateRequest
import com.example.products.ProductUpdateRequest
import com.example.repos.ProductRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(repo: ProductRepository) {
    route("/products") {
        get {
            val categoryId = call.request.queryParameters["category"]?.toIntOrNull()
            val search = call.request.queryParameters["search"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            val (items, total) = repo.findAll(categoryId, search, page, limit)
            call.respond(PageResponse(items, page, limit, total))
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("плохой id")
            val p = repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(p)
        }

        authenticate("auth-jwt") {
            post {
                requireAdmin() ?: return@post
                val body = call.receive<ProductCreateRequest>()
                val created = repo.create(body)
                call.respond(HttpStatusCode.Created, created)
            }
            put("/{id}") {
                requireAdmin() ?: return@put
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("плохой id")
                val body = call.receive<ProductUpdateRequest>()
                val updated = repo.update(id, body)
                if (updated == null) call.respond(HttpStatusCode.NotFound)
                else call.respond(updated)
            }
            delete("/{id}") {
                requireAdmin() ?: return@delete
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("плохой id")
                if (repo.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private suspend fun io.ktor.server.routing.RoutingContext.requireAdmin(): Unit? {
    val role = call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()
    if (role != "ADMIN") {
        call.respond(HttpStatusCode.Forbidden)
        return null
    }
    return Unit
}

package com.example.routes

import com.example.products.PageResponse
import com.example.repos.ProductRepository
import io.ktor.http.*
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
    }
}

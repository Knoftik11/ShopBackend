package com.example.routes

import com.example.repos.CategoryRepository
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(repo: CategoryRepository) {
    get("/categories") {
        call.respond(repo.findAll())
    }
}

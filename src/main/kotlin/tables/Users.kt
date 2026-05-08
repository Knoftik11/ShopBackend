package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : IntIdTable("users") {
    val name = varchar("name", 120)
    val email = varchar("email", 200).uniqueIndex()
    val passwordHash = varchar("password_hash", 200)
    val role = varchar("role", 20).default("USER")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

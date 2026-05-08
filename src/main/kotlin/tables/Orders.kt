package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Orders : IntIdTable("orders") {
    val userId = reference("user_id", Users)
    val status = varchar("status", 20).default("PENDING")
    val total = decimal("total", 14, 2)
    val address = varchar("address", 500).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

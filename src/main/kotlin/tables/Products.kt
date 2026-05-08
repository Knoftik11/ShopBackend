package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Products : IntIdTable("products") {
    val name = varchar("name", 200)
    val description = text("description")
    val price = decimal("price", 12, 2)
    val stock = integer("stock").default(0)
    val categoryId = reference("category_id", Categories).nullable()
    val imageUrl = varchar("image_url", 500).nullable()
}

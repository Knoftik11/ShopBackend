package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CartItems : IntIdTable("cart_items") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity").default(1)

    init {
        uniqueIndex(userId, productId)
    }
}

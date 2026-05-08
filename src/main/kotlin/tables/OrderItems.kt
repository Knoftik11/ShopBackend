package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OrderItems : IntIdTable("order_items") {
    val orderId = reference("order_id", Orders, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", Products)
    val quantity = integer("quantity")
    val priceAtTime = decimal("price_at_time", 12, 2)
}

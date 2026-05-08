package com.example.repos

import com.example.cart.CartDto
import com.example.cart.CartItemDto
import com.example.tables.CartItems
import com.example.tables.Products
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class CartRepository {

    fun getByUser(userId: Int): CartDto = transaction {
        val rows = (CartItems innerJoin Products)
            .selectAll()
            .where { CartItems.userId eq userId }
            .map { r ->
                CartItemDto(
                    id = r[CartItems.id].value,
                    productId = r[Products.id].value,
                    name = r[Products.name],
                    price = r[Products.price].toPlainString(),
                    imageUrl = r[Products.imageUrl],
                    quantity = r[CartItems.quantity],
                    stock = r[Products.stock]
                )
            }
        var total = BigDecimal.ZERO
        for (it in rows) {
            total = total + BigDecimal(it.price) * BigDecimal(it.quantity)
        }
        CartDto(items = rows, total = total.toPlainString())
    }

    fun addOrUpdate(userId: Int, productId: Int, quantity: Int) = transaction {
        val existing = CartItems.selectAll()
            .where { (CartItems.userId eq userId) and (CartItems.productId eq productId) }
            .limit(1)
            .firstOrNull()
        if (existing != null) {
            val newQty = existing[CartItems.quantity] + quantity
            CartItems.update({ CartItems.id eq existing[CartItems.id] }) {
                it[CartItems.quantity] = newQty.coerceAtLeast(1)
            }
        } else {
            CartItems.insert {
                it[CartItems.userId] = org.jetbrains.exposed.dao.id.EntityID(userId, com.example.tables.Users)
                it[CartItems.productId] = org.jetbrains.exposed.dao.id.EntityID(productId, Products)
                it[CartItems.quantity] = quantity.coerceAtLeast(1)
            }
        }
    }

    fun removeItem(userId: Int, itemId: Int): Boolean = transaction {
        CartItems.deleteWhere { (CartItems.id eq itemId) and (CartItems.userId eq userId) } > 0
    }

    fun clear(userId: Int) = transaction {
        CartItems.deleteWhere { CartItems.userId eq userId }
    }
}

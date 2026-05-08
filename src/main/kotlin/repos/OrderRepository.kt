package com.example.repos

import com.example.orders.OrderDto
import com.example.orders.OrderItemDto
import com.example.orders.OrderStatus
import com.example.tables.CartItems
import com.example.tables.OrderItems
import com.example.tables.Orders
import com.example.tables.Products
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

class OrderRepository {

    private val fmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun findByUser(userId: Int): List<OrderDto> = transaction {
        Orders.selectAll()
            .where { Orders.userId eq userId }
            .orderBy(Orders.createdAt to SortOrder.DESC)
            .map { r ->
                val orderId = r[Orders.id].value
                OrderDto(
                    id = orderId,
                    status = r[Orders.status],
                    total = r[Orders.total].toPlainString(),
                    address = r[Orders.address],
                    createdAt = r[Orders.createdAt].format(fmt),
                    items = loadItems(orderId)
                )
            }
    }

    fun findById(orderId: Int, userIdFilter: Int? = null): OrderDto? = transaction {
        val q = Orders.selectAll().where { Orders.id eq orderId }
        if (userIdFilter != null) {
            q.andWhere { Orders.userId eq userIdFilter }
        }
        q.limit(1).firstOrNull()?.let { r ->
            val id = r[Orders.id].value
            OrderDto(
                id = id,
                status = r[Orders.status],
                total = r[Orders.total].toPlainString(),
                address = r[Orders.address],
                createdAt = r[Orders.createdAt].format(fmt),
                items = loadItems(id)
            )
        }
    }

    fun updateStatus(orderId: Int, status: String): Boolean = transaction {
        require(status in OrderStatus.all) { "неизвестный статус: $status" }
        Orders.update({ Orders.id eq orderId }) {
            it[Orders.status] = status
        } > 0
    }

    fun createFromCart(userId: Int, address: String?): OrderDto = transaction {
        val cartRows = (CartItems innerJoin Products)
            .selectAll()
            .where { CartItems.userId eq userId }
            .toList()

        if (cartRows.isEmpty()) {
            throw IllegalArgumentException("корзина пуста")
        }

        var total = BigDecimal.ZERO
        for (row in cartRows) {
            val qty = row[CartItems.quantity]
            val stock = row[Products.stock]
            if (qty > stock) {
                val name = row[Products.name]
                throw IllegalArgumentException("товара \"$name\" недостаточно на складе")
            }
            total += row[Products.price] * qty.toBigDecimal()
        }

        val orderId = Orders.insertAndGetId {
            it[Orders.userId] = EntityID(userId, com.example.tables.Users)
            it[Orders.status] = OrderStatus.PENDING
            it[Orders.total] = total
            it[Orders.address] = address
        }.value

        for (row in cartRows) {
            val pid = row[Products.id].value
            val qty = row[CartItems.quantity]
            val price = row[Products.price]
            OrderItems.insert {
                it[OrderItems.orderId] = EntityID(orderId, Orders)
                it[OrderItems.productId] = EntityID(pid, Products)
                it[OrderItems.quantity] = qty
                it[OrderItems.priceAtTime] = price
            }
            Products.update({ Products.id eq pid }) {
                with(org.jetbrains.exposed.sql.SqlExpressionBuilder) {
                    it.update(Products.stock, Products.stock - qty)
                }
            }
        }

        CartItems.deleteWhere { CartItems.userId eq userId }

        findById(orderId) ?: error("заказ не создан")
    }

    private fun loadItems(orderId: Int): List<OrderItemDto> {
        return (OrderItems innerJoin Products)
            .selectAll()
            .where { OrderItems.orderId eq orderId }
            .map { row ->
                OrderItemDto(
                    productId = row[Products.id].value,
                    productName = row[Products.name],
                    quantity = row[OrderItems.quantity],
                    priceAtTime = row[OrderItems.priceAtTime].toPlainString()
                )
            }
    }
}

private fun org.jetbrains.exposed.sql.Query.andWhere(
    block: org.jetbrains.exposed.sql.SqlExpressionBuilder.() -> org.jetbrains.exposed.sql.Op<Boolean>
): org.jetbrains.exposed.sql.Query {
    val cond = org.jetbrains.exposed.sql.SqlExpressionBuilder.block()
    return adjustWhere { (this ?: org.jetbrains.exposed.sql.Op.TRUE) and cond }
}

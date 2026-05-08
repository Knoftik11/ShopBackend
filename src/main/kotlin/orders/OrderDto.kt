package com.example.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val priceAtTime: String
)

@Serializable
data class OrderDto(
    val id: Int,
    val status: String,
    val total: String,
    val address: String?,
    val createdAt: String,
    val items: List<OrderItemDto>
)

@Serializable
data class CreateOrderRequest(
    val address: String? = null
)

@Serializable
data class UpdateStatusRequest(
    val status: String
)

object OrderStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val SHIPPED = "SHIPPED"
    const val DELIVERED = "DELIVERED"
    const val CANCELLED = "CANCELLED"

    val all = listOf(PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
}

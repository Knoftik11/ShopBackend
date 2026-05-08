package com.example.cart

import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val id: Int,
    val productId: Int,
    val name: String,
    val price: String,
    val imageUrl: String?,
    val quantity: Int,
    val stock: Int
)

@Serializable
data class CartDto(
    val items: List<CartItemDto>,
    val total: String
)

@Serializable
data class AddToCartRequest(
    val productId: Int,
    val quantity: Int = 1
)

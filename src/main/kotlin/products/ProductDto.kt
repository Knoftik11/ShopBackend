package com.example.products

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    val stock: Int,
    val categoryId: Int?,
    val imageUrl: String?
)

@Serializable
data class ProductCreateRequest(
    val name: String,
    val description: String = "",
    val price: String,
    val stock: Int = 0,
    val categoryId: Int? = null,
    val imageUrl: String? = null
)

@Serializable
data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val stock: Int? = null,
    val categoryId: Int? = null,
    val imageUrl: String? = null
)

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val total: Long
)

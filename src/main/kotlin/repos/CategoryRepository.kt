package com.example.repos

import com.example.products.CategoryDto
import com.example.tables.Categories
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepository {

    fun findAll(): List<CategoryDto> = transaction {
        Categories.selectAll().map(::toDto)
    }

    private fun toDto(r: ResultRow) = CategoryDto(
        id = r[Categories.id].value,
        name = r[Categories.name],
        parentId = r[Categories.parentId]?.value
    )
}

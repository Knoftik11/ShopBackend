package com.example.repos

import com.example.products.ProductDto
import com.example.tables.Products
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ProductRepository {

    fun findAll(
        categoryId: Int? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Pair<List<ProductDto>, Long> = transaction {
        val q = Products.selectAll()
        if (categoryId != null) {
            q.andWhere { Products.categoryId eq categoryId }
        }
        if (!search.isNullOrBlank()) {
            val needle = "%${search.trim().lowercase()}%"
            q.andWhere { Products.name.lowerCase() like needle }
        }
        val total = q.count()
        val safePage = page.coerceAtLeast(1)
        val safeLimit = limit.coerceIn(1, 100)
        val items = q.limit(safeLimit).offset(((safePage - 1) * safeLimit).toLong())
            .map(::toDto)
        items to total
    }

    fun findById(id: Int): ProductDto? = transaction {
        Products.selectAll().where { Products.id eq id }
            .limit(1)
            .map(::toDto)
            .firstOrNull()
    }

    private fun toDto(r: ResultRow) = ProductDto(
        id = r[Products.id].value,
        name = r[Products.name],
        description = r[Products.description],
        price = r[Products.price].toPlainString(),
        stock = r[Products.stock],
        categoryId = r[Products.categoryId]?.value,
        imageUrl = r[Products.imageUrl]
    )
}

private fun org.jetbrains.exposed.sql.Query.andWhere(
    block: org.jetbrains.exposed.sql.SqlExpressionBuilder.() -> org.jetbrains.exposed.sql.Op<Boolean>
): org.jetbrains.exposed.sql.Query {
    val cond = org.jetbrains.exposed.sql.SqlExpressionBuilder.block()
    return adjustWhere { (this ?: org.jetbrains.exposed.sql.Op.TRUE) and cond }
}

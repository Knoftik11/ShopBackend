package com.example.repos

import com.example.products.ProductCreateRequest
import com.example.products.ProductDto
import com.example.products.ProductUpdateRequest
import com.example.tables.Products
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

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

    fun create(req: ProductCreateRequest): ProductDto = transaction {
        val newId = Products.insertAndGetId {
            it[name] = req.name
            it[description] = req.description
            it[price] = BigDecimal(req.price)
            it[stock] = req.stock
            it[categoryId] = req.categoryId?.let { cid -> EntityID(cid, com.example.tables.Categories) }
            it[imageUrl] = req.imageUrl
        }.value
        findById(newId) ?: error("ошибка создания товара")
    }

    fun update(id: Int, req: ProductUpdateRequest): ProductDto? = transaction {
        val n = Products.update({ Products.id eq id }) { row ->
            req.name?.let { row[name] = it }
            req.description?.let { row[description] = it }
            req.price?.let { row[price] = BigDecimal(it) }
            req.stock?.let { row[stock] = it }
            if (req.categoryId != null) row[categoryId] = EntityID(req.categoryId, com.example.tables.Categories)
            req.imageUrl?.let { row[imageUrl] = it }
        }
        if (n == 0) null else findById(id)
    }

    fun delete(id: Int): Boolean = transaction {
        Products.deleteWhere { Products.id eq id } > 0
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

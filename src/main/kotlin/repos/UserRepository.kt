package com.example.repos

import com.example.auth.UserDto
import com.example.tables.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    fun findByEmail(email: String): UserRow? = transaction {
        Users.selectAll().where { Users.email eq email }
            .limit(1)
            .map(::toRow)
            .firstOrNull()
    }

    fun findById(id: Int): UserRow? = transaction {
        Users.selectAll().where { Users.id eq id }
            .limit(1)
            .map(::toRow)
            .firstOrNull()
    }

    fun create(name: String, email: String, passwordHash: String, role: String = "USER"): UserRow = transaction {
        val newId = Users.insertAndGetId {
            it[Users.name] = name
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.role] = role
        }.value
        findById(newId) ?: error("user not created")
    }

    private fun toRow(r: ResultRow) = UserRow(
        id = r[Users.id].value,
        name = r[Users.name],
        email = r[Users.email],
        passwordHash = r[Users.passwordHash],
        role = r[Users.role]
    )
}

data class UserRow(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String
) {
    fun toDto() = UserDto(id = id, name = name, email = email, role = role)
}

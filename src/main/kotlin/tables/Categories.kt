package com.example.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Categories : IntIdTable("categories") {
    val name = varchar("name", 120)
    val parentId = reference("parent_id", Categories).nullable()
}

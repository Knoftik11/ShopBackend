package com.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val cfg = environment.config.config("db")
    val hikari = HikariConfig().apply {
        jdbcUrl = cfg.property("jdbcUrl").getString()
        username = cfg.property("user").getString()
        password = cfg.property("password").getString()
        driverClassName = cfg.property("driver").getString()
        maximumPoolSize = cfg.property("maxPoolSize").getString().toInt()
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val ds = HikariDataSource(hikari)
    Database.connect(ds)
    log.info("DB connected: ${cfg.property("jdbcUrl").getString()}")
}

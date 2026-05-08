package com.example

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class ServerTest {

    @Test
    fun `ping returns ok`() = testApplication {
        environment { config = ApplicationConfig("application.yaml") }
        application {
            configureSerialization()
            configureHTTP()
            configureSecurity()
            configureRouting()
        }
        val resp = client.get("/ping")
        assertEquals(HttpStatusCode.OK, resp.status)
    }
}

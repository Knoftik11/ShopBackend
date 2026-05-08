package com.example

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.*

class ServerTest {

    @Test
    fun `ping returns ok`() = testApplication {
        application {
            configureSerialization()
            configureHTTP()
            configureRouting()
        }
        val resp = client.get("/ping")
        assertEquals(HttpStatusCode.OK, resp.status)
    }
}

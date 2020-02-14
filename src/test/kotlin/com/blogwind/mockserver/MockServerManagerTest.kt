package com.blogwind.mockserver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException

class MockServerManagerTest {
    @Test
    fun testDefaultServer() {
        MockServerManager.setDefaultResponse("/", "ok")

        val client: WebClient = WebClient.create(MockServerManager.getUrl())
        val result = client.get()
            .uri { it.path("/").build()}
            .retrieve()
            .onStatus({ status -> !status.is2xxSuccessful}) {
                val status = it.statusCode()
                it.bodyToMono(String::class.java)
                    .map { ResponseStatusException(status, it) }
            }
            .bodyToMono(String::class.java)

        Assertions.assertEquals(result.block(), "ok")
    }
}
package com.blogwind.mockserver

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException

class MockServerManagerTest {
    @Test
    fun testDefaultServer() {
        var user = TestUser("test", "run", 18)
        MockServerManager.setDefaultJsonResponse("/", user)

        val client: WebClient = WebClient.create(MockServerManager.getUrl())
        val result = client.get()
            .uri { it.path("/").build() }
            .retrieve()
            .onStatus({ status -> !status.is2xxSuccessful }) {
                val status = it.statusCode()
                it.bodyToMono(String::class.java)
                    .map { ResponseStatusException(status, it) }
            }
            .bodyToMono(TestUser::class.java)


        val user2 = result.block()!!

        Assertions.assertEquals(user.name, user2.name)
    }
}
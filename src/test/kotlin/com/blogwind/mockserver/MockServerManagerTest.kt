package com.blogwind.mockserver

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URL

class MockServerManagerTest {
    @Test
    fun testDefaultServer() {
        var user = TestUser("test", "run", 18)
        MockServerManager.setDefaultJsonResponse("/", user)

        val result = URL(MockServerManager.getUrl()).readText()

        val user2: TestUser = jacksonObjectMapper().readValue(result)

        Assertions.assertEquals(user.name, user2.name)
    }
}
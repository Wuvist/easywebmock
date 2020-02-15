package com.blogwind.mockserver

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URL

class MockServerManagerTest {
    @Test
    fun testDefaultServer() {
        var user = TestUser("test", "run", 18)
        MockServerManager.setDefaultJsonResponse("/", user)

        val client = UrlClient(MockServerManager.getUrl())

        val user2: TestUser = client.getObject("/")

        Assertions.assertEquals(user.name, user2.name)
    }

    @Test
    fun testHandler() {
        var user = TestUser("test", "run", 18)
        MockServerManager.setDefaultResponse("/user", fun(request : RecordedRequest) : MockResponse {
            val input = request.body.readUtf8()
            var mapper = jacksonObjectMapper()
            var user: TestUser = mapper.readValue(input)
            user.age = (user.age?: 0) * 2

            return MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(user))
        })

        val client = UrlClient(MockServerManager.getUrl())
        val user2: TestUser = client.postObject("/user", user)

        Assertions.assertEquals(user2.age, 36)
    }
}
package com.blogwind.easywebmock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.IOException

class MockServerManagerTest {
    @Test
    fun testDefaultServer() {
        var user = TestUser("test", "run", 18)
        MockServerManager.setDefaultResponseJson("/", user)

        val client = UrlClient(MockServerManager.getUrl())

        val user2: TestUser = client.getForObject("/")

        Assertions.assertEquals(user.name, user2.name)
    }

    @Test
    fun testHandler() {
        var user = TestUser("test", "run", 18)
        MockServerManager.setDefaultResponse("/user", fun(request: RecordedRequest): MockResponse {
            val input = request.body.readUtf8()
            var mapper = jacksonObjectMapper()
            var resp: TestUser = mapper.readValue(input)
            resp.age = (resp.age ?: 0) * 2

            return MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(resp))
        })

        val client = UrlClient(MockServerManager.getUrl())
        val user2: TestUser = client.postForObject("/user", user)

        Assertions.assertEquals(user2.age, 36)
    }

    @Test
    fun testHandlerError() {
        val path = "/error"
        MockServerManager.setDefaultResponse(path, fun(request: RecordedRequest): MockResponse {
            val input = request.body.readUtf8()
            var mapper = jacksonObjectMapper()
            var resp: TestUser = mapper.readValue(input)

            return MockResponse().setResponseCode(200)
                .setBody(mapper.writeValueAsString(resp))
        })

        val client = UrlClient(MockServerManager.getUrl())

        var flag = false
        try {
            client.getForObject<TestUser>(path)
        } catch (e: IOException) {
            flag = true
        }
        Assertions.assertTrue(flag)
    }

    @Test
    fun testOneTime() {
        MockServerManager.newServer("test")
        val server = MockServerManager.getServer("test")!!
        val client = UrlClient(server.getUrl())

        server.setDefaultResponse("/ping", "ok")
        server.setOneTimeResponse("/ping", "ping")
        server.setOneTimeResponse("/ping", "pong")

        Assertions.assertEquals(client.get("/ping"), "ping")
        Assertions.assertEquals(client.get("/ping"), "pong")
        Assertions.assertEquals(client.get("/ping"), "ok")
    }

    @Test
    fun testOneTimeJson() {
        var user = TestUser("test", "run", 18)
        var user2 = TestUser("test", "run", 19)
        val toPath = "/test_user"
        val client = UrlClient(MockServerManager.getUrl())

        MockServerManager.setDefaultResponseJson(toPath, user)
        MockServerManager.setOneTimeResponseJson(toPath, user2)

        Assertions.assertEquals(client.getForObject<TestUser>(toPath).age, user2.age)
        Assertions.assertEquals(client.getForObject<TestUser>(toPath).age, user.age)
    }

    @Test
    fun testIsRunning() {
        val client = UrlClient(MockServerManager.getUrl())
        val toPath = "/status"
        MockServerManager.setRunning(false)

        MockServerManager.setDefaultResponse(toPath, "up")
        MockServerManager.setOneTimeResponse(toPath, "ok")
        var flag = false
        try {
            client.get(toPath)
        } catch (e: IOException) {
            flag = true
        }

        Assertions.assertTrue(flag)

        MockServerManager.setRunning(true)
        Assertions.assertEquals(client.get(toPath), "ok")
        Assertions.assertEquals(client.get(toPath), "up")
    }

    @Test
    fun testResponse() {
        val client = UrlClient(MockServerManager.getUrl())
        val toPath = "/resp"

        MockServerManager.setDefaultResponse(
            toPath, MockResponse().setResponseCode(200)
                .setBody("ping")
        )
        MockServerManager.setOneTimeResponse(
            toPath, MockResponse().setResponseCode(200)
                .setBody("pong")
        )

        Assertions.assertEquals(client.get(toPath), "pong")
        Assertions.assertEquals(client.get(toPath), "ping")
    }

    @Test
    fun testHTML() {
        val server = MockServerManager.defaultServer
        val client = UrlClient(server.getUrl())
        val toPath = "/html"

        var flag = false
        try {
            client.get(toPath)
        } catch (e: IOException) {
            flag = true
        }
        Assertions.assertTrue(flag)

        server.setDefaultResponse(toPath, "<html></html>", "text/html")
        Assertions.assertEquals(client.get(toPath), "<html></html>")
    }

    @Test
    fun testPath() {
        val server = MockServerManager.defaultServer
        val client = UrlClient(server.getUrl())
        val toPath = "/foobar"

        server.setOneTimeResponse(toPath, "foobar")
        Assertions.assertEquals(client.get("$toPath?user=1"), "foobar")
    }

    @Test
    fun testMisc() {
        val server = MockServerManager()

        Assertions.assertNotNull(server)
        Assertions.assertNotNull(MockServerManager.defaultServer.server)
    }
}
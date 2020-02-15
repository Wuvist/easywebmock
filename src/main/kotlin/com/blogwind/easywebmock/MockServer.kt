package com.blogwind.easywebmock

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

data class TestUser(val name: String?, val email: String?, var age: Int?)

class MockServer {
    val server: MockWebServer = MockWebServer()
    private var isRunning: Boolean = true
    private var oneTimeResponses = mutableMapOf<String, Stack<MockResponse>>()
    private var defaultResponses = mutableMapOf<String, MockResponse>()
    private var defaultHandlers = mutableMapOf<String, (RecordedRequest) -> MockResponse>()

    fun setDefaultResponse(toPath: String, withResponse: MockResponse): MockServer {
        defaultResponses[toPath] = withResponse
        return this
    }

    fun setDefaultResponse(toPath: String, withString: String, contentType: String = "text/plain"): MockServer {
        setDefaultResponse(toPath, getStringResp(withString, contentType))
        return this
    }

    fun setDefaultJsonResponse(toPath: String, withObject: Any): MockServer {
        setDefaultResponse(toPath, getJsonResp(withObject))
        return this
    }

    fun setDefaultResponse(toPath: String, withHandler: (RecordedRequest) -> MockResponse): MockServer {
        defaultHandlers[toPath] = withHandler
        return this
    }

    fun setOneTimeResponse(toPath: String, withResponse: MockResponse): MockServer {
        var respStack = oneTimeResponses[toPath]
        if (respStack == null) {
            respStack = Stack()
            oneTimeResponses[toPath] = respStack
        }
        respStack.push(withResponse)
        return this
    }

    fun setOneTimeResponse(toPath: String, withString: String, contentType: String = "text/plain"): MockServer {
        setOneTimeResponse(toPath, getStringResp(withString, contentType))
        return this
    }

    fun setOneTimeJsonResponse(toPath: String, withObject: Any): MockServer {
        setOneTimeResponse(toPath, getJsonResp(withObject))
        return this
    }

    fun getUrl(): String {
        return server.url("/").toString()
    }

    fun setRunning(flag: Boolean) {
        isRunning = flag
    }

    private fun getResponseOnce(forPath: String): MockResponse? {
        var respStack = oneTimeResponses[forPath] ?: return null

        val resp = respStack.pop()

        if (respStack.isEmpty()) {
            oneTimeResponses.remove(forPath)
        }

        return resp
    }

    init {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (!isRunning) {
                    return MockResponse().setResponseCode(500).addHeader("Content-Type", "text/plain")
                        .setBody("Internal error")
                }

                var resp = getResponseOnce(request.path)
                if (resp != null) {
                    return resp
                }

                resp = defaultResponses[request.path]
                if (resp != null) {
                    return resp
                }

                val handler = defaultHandlers[request.path]
                if (handler != null) {
                    return try {
                        handler(request)
                    } catch (e: Exception) {
                        println("Handler Error :\n$e")

                        MockResponse().setResponseCode(500)
                            .addHeader("Content-Type", "text/plain")
                            .setBody("Handler Error :\n$e")
                    }
                }

                return MockResponse().setResponseCode(404)
                    .addHeader("Content-Type", "application/json")
                    .setBody("Not Found")
            }
        }
        server.setDispatcher(dispatcher)
        server.start()
    }

    private fun getJsonResp(obj: Any): MockResponse {
        val objectMapper = ObjectMapper()
        return MockResponse().setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(objectMapper.writeValueAsString(obj))
    }

    private fun getStringResp(resp: String, contentType: String): MockResponse {
        return MockResponse().setResponseCode(200)
            .addHeader("Content-Type", contentType)
            .setBody(resp)
    }
}
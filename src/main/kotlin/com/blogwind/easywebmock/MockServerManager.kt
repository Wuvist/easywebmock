package com.blogwind.easywebmock

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockServerManager {
    companion object {
        val defaultServer: MockServer = MockServer()
        private var servers = mutableMapOf<String, MockServer>()

        fun newServer(name: String): MockServer {
            val server = MockServer()
            servers[name] = server
            return server
        }

        fun getServer(name: String): MockServer? {
            return servers[name]
        }

        fun setDefaultResponse(toPath: String, withResponse: MockResponse): MockServer {
            return defaultServer.setDefaultResponse(toPath, withResponse)
        }

        fun setDefaultResponse(toPath: String, withHandler: (RecordedRequest) -> MockResponse): MockServer {
            return defaultServer.setDefaultResponse(toPath, withHandler)
        }

        fun setDefaultResponse(toPath: String, withString: String, contentType: String = ""): MockServer {
            return defaultServer.setDefaultResponse(toPath, withString, contentType)
        }

        fun setDefaultResponseJson(toPath: String, withObject: Any): MockServer {
            return defaultServer.setDefaultResponseJson(toPath, withObject)
        }

        fun setOneTimeResponse(toPath: String, withResponse: MockResponse): MockServer {
            return defaultServer.setOneTimeResponse(toPath, withResponse)
        }

        fun setOneTimeResponse(toPath: String, withString: String, contentType: String = ""): MockServer {
            return defaultServer.setOneTimeResponse(toPath, withString, contentType)
        }

        fun setOneTimeResponseJson(toPath: String, withObject: Any): MockServer {
            return defaultServer.setOneTimeResponseJson(toPath, withObject)
        }

        fun getUrl(): String {
            return defaultServer.getUrl()
        }

        fun setRunning(flag: Boolean) {
            defaultServer.setRunning(flag)
        }
    }
}
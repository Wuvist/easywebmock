package com.blogwind.easywebmock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URL

class UrlClient(baseUrl: String) {
    private val baseUrl = baseUrl.trimEnd('/')

    fun get(path: String): String {
        return URL(baseUrl + path).readText()
    }

    inline fun <reified T> getObject(path: String): T {
        return jacksonObjectMapper().readValue(get(path))
    }

    fun post(path: String, content: String = ""): String {
        val client = OkHttpClient()
        val contentType = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(contentType, content)

        val request: Request = Request.Builder()
            .url(baseUrl + path)
            .post(body).build()

        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    inline fun <reified T> postObject(path: String, obj: Any): T {
        val objectMapper = ObjectMapper()
        val content = objectMapper.writeValueAsString(obj)
        return postObject(path, content)
    }

    inline fun <reified T> postObject(path: String, content: String = ""): T {
        return jacksonObjectMapper().readValue(post(path, content))
    }
}
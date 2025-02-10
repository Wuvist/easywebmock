package com.blogwind.easywebmock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

class UrlClient(baseUrl: String) {
    private val baseUrl = baseUrl.trimEnd('/')

    fun get(path: String): String {
        return URI(baseUrl + path).toURL().readText()
    }

    inline fun <reified T> getForObject(path: String): T {
        return jacksonObjectMapper().readValue(get(path))
    }

    fun post(path: String, content: String = ""): String {
        val client = OkHttpClient()
        val contentType = "application/json; charset=utf-8".toMediaType()
        val body = content.toRequestBody(contentType)

        val request: Request = Request.Builder()
            .url(baseUrl + path)
            .post(body).build()

        val response = client.newCall(request).execute()
        return response.body!!.string()
    }

    inline fun <reified T> postForObject(path: String, obj: Any): T {
        val objectMapper = ObjectMapper()
        val content = objectMapper.writeValueAsString(obj)
        return postForObject(path, content)
    }

    inline fun <reified T> postForObject(path: String, content: String = ""): T {
        return jacksonObjectMapper().readValue(post(path, content))
    }
}
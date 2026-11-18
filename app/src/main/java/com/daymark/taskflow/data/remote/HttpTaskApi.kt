
package com.daymark.taskflow.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpTaskApi(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val client: OkHttpClient = defaultClient(),
    private val gson: Gson = Gson(),
) : RemoteTaskApi {
    private val endpoint = baseUrl.trimEnd('/') + "/todos?_limit=5"

    override suspend fun fetchTodos(): List<RemoteTodo> =
        withContext(Dispatchers.IO) {
            val request =
                Request
                    .Builder()
                    .url(endpoint)
                    .get()
                    .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw HttpStatusException(response.code)
                }

                val body =
                    response.body?.string()
                        ?: throw IOException("Empty response body")

                try {
                    val type = object : TypeToken<List<RemoteTodo>>() {}.type
                    gson.fromJson<List<RemoteTodo>>(body, type) ?: emptyList()
                } catch (error: JsonParseException) {
                    throw IOException("Invalid JSON from server", error)
                }
            }
        }

    companion object {
        const val DEFAULT_BASE_URL =
            "https://jsonplaceholder.typicode.com"

        fun defaultClient(): OkHttpClient =
            OkHttpClient
                .Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .callTimeout(12, TimeUnit.SECONDS)
                .build()
    }
}

class HttpStatusException(
    val statusCode: Int,
) : IOException("Server returned HTTP $statusCode")

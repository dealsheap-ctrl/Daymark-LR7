package com.daymark.taskflow.data

import com.daymark.taskflow.data.remote.HttpStatusException
import com.daymark.taskflow.data.remote.HttpTaskApi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpTaskApiTest {
    private lateinit var server: MockWebServer

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After fun tearDown() {
        server.shutdown()
    }

    @Test fun parsesSuccessfulResponse() =
        runBlocking {
            server.enqueue(
                MockResponse()
                    .setResponseCode(
                        200,
                    ).setBody("""[{"userId":1,"id":12,"title":"check inbox","completed":false}]"""),
            )
            val result = HttpTaskApi(server.url("/").toString(), HttpTaskApi.defaultClient()).fetchTodos()
            assertEquals(1, result.size)
            assertEquals("check inbox", result.first().title)
            assertEquals("/todos?_limit=5", server.takeRequest().path)
        }

    @Test fun reports404() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(404).setBody("not found"))
            val error = runCatching { HttpTaskApi(server.url("/").toString()).fetchTodos() }.exceptionOrNull()
            assertTrue(error is HttpStatusException)
            assertEquals(404, (error as HttpStatusException).statusCode)
        }

    @Test fun reports500() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(500).setBody("server error"))
            val error = runCatching { HttpTaskApi(server.url("/").toString()).fetchTodos() }.exceptionOrNull()
            assertTrue(error is HttpStatusException)
            assertEquals(500, (error as HttpStatusException).statusCode)
        }

    @Test fun reportsMalformedJson() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("{not-json"))
            val error = runCatching { HttpTaskApi(server.url("/").toString()).fetchTodos() }.exceptionOrNull()
            assertTrue(error is IOException)
        }

    @Test fun readTimeoutIsHandled() =
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(200).setBody("[]").setBodyDelay(400, TimeUnit.MILLISECONDS))
            val client =
                OkHttpClient
                    .Builder()
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .readTimeout(80, TimeUnit.MILLISECONDS)
                    .callTimeout(1, TimeUnit.SECONDS)
                    .build()
            val error = runCatching { HttpTaskApi(server.url("/").toString(), client).fetchTodos() }.exceptionOrNull()
            assertTrue("Expected an IOException timeout, got $error", error is IOException)
        }
}

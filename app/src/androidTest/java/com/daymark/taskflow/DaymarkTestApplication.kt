package com.daymark.taskflow

import com.daymark.taskflow.data.remote.RemoteTaskApi
import com.daymark.taskflow.data.remote.RemoteTodo

/** A deterministic API implementation keeps instrumented UI tests independent of the internet. */
class DaymarkTestApplication : DaymarkApplication() {
    override fun createRemoteApi() =
        object {
            override suspend fun fetchTodos(): List<RemoteTodo> =
                listOf(
                    RemoteTodo(userId = 1, id = 9001, title = "UI test imported task", completed = false),
                    RemoteTodo(userId = 1, id = 9002, title = "Second UI test task", completed = true),
                )
        }
}

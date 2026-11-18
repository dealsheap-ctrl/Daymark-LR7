package com.daymark.taskflow.data.remote

interface RemoteTaskApi {
    suspend fun fetchTodos(): List<RemoteTodo>
}

data class RemoteTodo(
    val userId: Int = 0,
    val id: Int = 0,
    val title: String = "",
    val completed: Boolean = false,
)

package com.daymark.taskflow.data.repository

import com.daymark.taskflow.data.local.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>

    suspend fun add(task: Task): Long

    suspend fun setCompleted(
        id: Long,
        completed: Boolean,
    )

    suspend fun delete(id: Long)

    suspend fun deleteCompleted()

    suspend fun count(): Int

    suspend fun syncRemote(): Int
}

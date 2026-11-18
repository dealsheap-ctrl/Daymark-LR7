package com.daymark.taskflow.data

import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Small working in-memory substitute used when a test needs behavior, not interaction verification. */
internal class InMemoryTaskRepositoryFake : TaskRepository {
    private val state = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override fun observeTasks(): Flow<List<Task>> = state

    override suspend fun add(task: Task): Long {
        val stored = task.copy(id = nextId++)
        state.value = state.value + stored
        return stored.id
    }

    override suspend fun setCompleted(
        id: Long,
        completed: Boolean,
    ) {
        state.value = state.value.map { if (it.id == id) it.copy(isCompleted = completed) else it }
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun deleteCompleted() {
        state.value = state.value.filterNot { it.isCompleted }
    }

    override suspend fun count(): Int = state.value.size

    override suspend fun syncRemote(): Int = 0
}

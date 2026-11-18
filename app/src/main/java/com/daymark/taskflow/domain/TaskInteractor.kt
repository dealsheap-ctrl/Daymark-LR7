package com.daymark.taskflow.domain

import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.data.repository.TaskRepository

sealed interface AddTaskOutcome {
    data class Saved(
        val id: Long,
    ) : AddTaskOutcome

    data class Invalid(
        val reason: String,
    ) : AddTaskOutcome
}

class TaskInteractor(
    private val repository: TaskRepository,
) {
    suspend fun addTask(
        title: String,
        note: String,
        priority: String,
        dueAt: Long,
    ): AddTaskOutcome {
        val validation = TaskValidator.validateTitle(title)
        if (!validation.isValid) return AddTaskOutcome.Invalid(validation.message ?: "Invalid title")
        val safePriority = priority.uppercase().takeIf { it in setOf("LOW", "NORMAL", "HIGH") } ?: "NORMAL"
        val task =
            Task(
                title = validation.normalizedValue,
                note = TaskValidator.normalizeNote(note),
                priority = safePriority,
                dueAt = dueAt,
            )
        return AddTaskOutcome.Saved(repository.add(task))
    }

    suspend fun toggleTask(
        id: Long,
        completed: Boolean,
    ) = repository.setCompleted(id, completed)

    suspend fun deleteTask(id: Long) = repository.delete(id)

    suspend fun deleteCompleted() = repository.deleteCompleted()

    suspend fun syncRemote(): Int = repository.syncRemote()
}

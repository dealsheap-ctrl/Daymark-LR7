package com.daymark.taskflow.data.repository

import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.data.local.TaskDao
import com.daymark.taskflow.data.local.TaskEntity
import com.daymark.taskflow.data.local.toDomain
import com.daymark.taskflow.data.local.toEntity
import com.daymark.taskflow.data.remote.RemoteTaskApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class DefaultTaskRepository(
    private val dao: TaskDao,
    private val remoteApi: RemoteTaskApi
) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun add(task: Task): Long = dao.insert(task.toEntity())

    override suspend fun setCompleted(id: Long, completed: Boolean) = dao.setCompleted(id, completed)

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun deleteCompleted() = dao.deleteCompleted()

    override suspend fun count(): Int = dao.count()

    override suspend fun syncRemote(): Int {
        val remote = remoteApi.fetchTodos()
        val now = System.currentTimeMillis()
        val rows = mutableListOf<TaskEntity>()
        remote.take(5).forEachIndexed { index, todo ->
            // A stable remoteId lets later Sync operations update an imported row instead
            // of creating another copy of the same remote task. Keep this loop explicit:
            // DAO lookup is suspendable and must complete before insertAll is called.
            val existing = dao.findByRemoteId(todo.id)
            rows += Task(
                id = existing?.id ?: 0L,
                title = todo.title.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                note = existing?.note ?: "Imported from the demo REST service",
                priority = existing?.priority ?: if (index == 0) "HIGH" else "LOW",
                dueAt = existing?.dueAt ?: now + (index + 1) * 86_400_000L,
                isCompleted = todo.completed,
                createdAt = existing?.createdAt ?: now,
                isPinned = existing?.isPinned ?: false,
                remoteId = todo.id
            ).toEntity()
        }
        if (rows.isNotEmpty()) dao.insertAll(rows)
        return rows.size
    }
}

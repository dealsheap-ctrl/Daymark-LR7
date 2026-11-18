package com.daymark.taskflow.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String,
    val priority: String,
    val dueAt: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val isPinned: Boolean = false,
    val remoteId: Int? = null
)

data class Task(
    val id: Long = 0,
    val title: String,
    val note: String = "",
    val priority: String = "NORMAL",
    val dueAt: Long = System.currentTimeMillis() + 86_400_000L,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val remoteId: Int? = null
)

fun TaskEntity.toDomain() = Task(id, title, note, priority, dueAt, isCompleted, createdAt, isPinned, remoteId)
fun Task.toEntity() = TaskEntity(id, title, note, priority, dueAt, isCompleted, createdAt, isPinned, remoteId)

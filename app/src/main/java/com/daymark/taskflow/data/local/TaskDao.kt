package com.daymark.taskflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueAt ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>): List<Long>

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: Int): TaskEntity?
}

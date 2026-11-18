package com.daymark.taskflow.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.daymark.taskflow.data.local.TaskDatabase
import com.daymark.taskflow.data.local.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoInstrumentedTest {
    private lateinit var database: TaskDatabase

    @Before fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, TaskDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After fun closeDatabase() {
        database.close()
    }

    @Test fun insertObserveCompleteAndDeleteTask() =
        runBlocking {
            val dao = database.taskDao()
            val id =
                dao.insert(
                    TaskEntity(title = "Run DAO test", note = "in memory", priority = "NORMAL", dueAt = 10L),
                )
            assertEquals(1, dao.count())
            assertEquals(
                "Run DAO test",
                dao
                    .observeAll()
                    .first()
                    .single()
                    .title,
            )
            dao.setCompleted(id, true)
            assertTrue(
                dao
                    .observeAll()
                    .first()
                    .single()
                    .isCompleted,
            )
            dao.delete(id)
            assertEquals(0, dao.count())
        }

    @Test fun deleteCompletedLeavesOpenTasks() =
        runBlocking {
            val dao = database.taskDao()
            dao.insertAll(
                listOf(
                    TaskEntity(title = "Done", note = "", priority = "LOW", dueAt = 1L, isCompleted = true),
                    TaskEntity(title = "Open", note = "", priority = "HIGH", dueAt = 2L, isCompleted = false),
                ),
            )
            dao.deleteCompleted()
            assertEquals(listOf("Open"), dao.observeAll().first().map { it.title })
        }
}

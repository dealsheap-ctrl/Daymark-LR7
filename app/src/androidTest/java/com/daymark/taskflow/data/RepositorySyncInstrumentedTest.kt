package com.daymark.taskflow.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.daymark.taskflow.data.local.TaskDatabase
import com.daymark.taskflow.data.remote.RemoteTaskApi
import com.daymark.taskflow.data.remote.RemoteTodo
import com.daymark.taskflow.data.repository.DefaultTaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositorySyncInstrumentedTest {
    private lateinit var database: TaskDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun repeatedSyncUpdatesImportedRowsInsteadOfCreatingDuplicates() = runBlocking {
        val api = object  {
            override suspend fun fetchTodos() = listOf(
                RemoteTodo(userId = 1, id = 701, title = "stable remote task", completed = false)
            )
        }
        val repository = DefaultTaskRepository(database.taskDao(), api)

        assertEquals(1, repository.syncRemote())
        val firstLocalId = database.taskDao().findByRemoteId(701)?.id
        assertEquals(1, repository.syncRemote())

        val rows = database.taskDao().observeAll().first()
        assertEquals(1, rows.size)
        assertEquals(firstLocalId, rows.single().id)
        assertEquals(701, rows.single().remoteId)
    }
}

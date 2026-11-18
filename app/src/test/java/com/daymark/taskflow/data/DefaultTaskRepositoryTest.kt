package com.daymark.taskflow.data

import com.daymark.taskflow.data.local.TaskDao
import com.daymark.taskflow.data.local.TaskEntity
import com.daymark.taskflow.data.remote.RemoteTaskApi
import com.daymark.taskflow.data.remote.RemoteTodo
import com.daymark.taskflow.data.repository.DefaultTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class DefaultTaskRepositoryTest {
    @Test fun previouslyImportedRemoteItemReusesItsLocalId() =
        runTest {
            val dao = mock<TaskDao>()
            val api = mock<RemoteTaskApi>()
            val existing =
                TaskEntity(
                    id = 77L,
                    title = "Old title",
                    note = "Keep local note",
                    priority = "HIGH",
                    dueAt = 123L,
                    isCompleted = false,
                    createdAt = 100L,
                    isPinned = true,
                    remoteId = 42,
                )
            whenever(api.fetchTodos()).thenReturn(listOf(RemoteTodo(id = 42, title = "updated title")))
            whenever(dao.findByRemoteId(42)).thenReturn(existing)
            whenever(dao.insertAll(any())).thenReturn(listOf(77L))

            val imported = DefaultTaskRepository(dao, api).syncRemote()

            assertEquals(1, imported)
            verifyBlocking(dao) {
                insertAll(
                    check { rows ->
                        assertEquals(77L, rows.single().id)
                        assertEquals(42, rows.single().remoteId)
                        assertEquals("Keep local note", rows.single().note)
                        assertEquals(true, rows.single().isPinned)
                        assertEquals(123L, rows.single().dueAt)
                    },
                )
            }
        }

    @Test fun remoteItemsAreMappedAndStored() =
        runTest {
            val dao = mock<TaskDao>()
            val api = mock<RemoteTaskApi>()
            whenever(api.fetchTodos()).thenReturn(listOf(RemoteTodo(id = 1, title = "remote item", completed = true)))
            whenever(dao.insertAll(any())).thenReturn(listOf(7L))

            val imported = DefaultTaskRepository(dao, api).syncRemote()

            assertEquals(1, imported)
            verifyBlocking(api) { fetchTodos() }
            verifyBlocking(dao) {
                insertAll(
                    check { rows ->
                        assertEquals(1, rows.size)
                        assertEquals("Remote item", rows.first().title)
                        assertEquals(true, rows.first().isCompleted)
                        assertEquals("HIGH", rows.first().priority)
                    },
                )
            }
        }
}

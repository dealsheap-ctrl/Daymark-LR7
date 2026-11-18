package com.daymark.taskflow.domain

import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.data.repository.TaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class TaskInteractorTest {
    @Test fun validTaskIsSavedAndRepositoryIsCalled() =
        runTest {
            val repository = mock<TaskRepository>()
            whenever(repository.add(any())).thenReturn(42L)
            val interactor = TaskInteractor(repository)

            val result = interactor.addTask("  Call the supplier  ", "  ask for a quote  ", "high", 123L)

            assertEquals(AddTaskOutcome.Saved(42L), result)
            verifyBlocking(repository) {
                add(
                    check { task ->
                        assertEquals("Call the supplier", task.title)
                        assertEquals("ask for a quote", task.note)
                        assertEquals("HIGH", task.priority)
                        assertEquals(123L, task.dueAt)
                    },
                )
            }
        }

    @Test fun invalidTaskDoesNotTouchRepository() =
        runTest {
            val repository = mock<TaskRepository>()
            val result = TaskInteractor(repository).addTask("   ", "", "NORMAL", 1L)
            assertTrue(result is AddTaskOutcome.Invalid)
            verifyNoInteractions(repository)
        }

    @Test fun mutationAndSyncOperationsAreDelegated() =
        runTest {
            val repository = mock<TaskRepository>()
            whenever(repository.syncRemote()).thenReturn(3)
            val interactor = TaskInteractor(repository)

            interactor.toggleTask(5L, true)
            interactor.deleteTask(5L)
            interactor.deleteCompleted()
            assertEquals(3, interactor.syncRemote())

            verifyBlocking(repository) { setCompleted(5L, true) }
            verifyBlocking(repository) { delete(5L) }
            verifyBlocking(repository) { deleteCompleted() }
            verifyBlocking(repository) { syncRemote() }
        }

    @Test fun unknownPriorityFallsBackToNormal() =
        runTest {
            val repository = mock<TaskRepository>()
            whenever(repository.add(any())).thenReturn(9L)
            TaskInteractor(repository).addTask("Review notes", "", "URGENT-PLUS", 2L)
            verifyBlocking(repository) { add(check { assertEquals("NORMAL", it.priority) }) }
        }
}

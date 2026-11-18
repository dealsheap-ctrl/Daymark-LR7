package com.daymark.taskflow

import com.daymark.taskflow.data.InMemoryTaskRepositoryFake
import com.daymark.taskflow.ui.UiMessage
import com.daymark.taskflow.ui.TasksViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun initialTasksAreSeededAndObservedThroughStateFlow() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryTaskRepositoryFake()
        val viewModel = TasksViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.tasks.collect { }
        }

        advanceUntilIdle()

        assertEquals(3, repository.count())
        assertEquals(3, viewModel.tasks.value.size)
        assertEquals(2, viewModel.tasks.value.count { !it.isCompleted })
        assertEquals(1, viewModel.tasks.value.count { it.isCompleted })
    }
    @Test fun addTaskUpdatesRepositoryAndPublishesUiMessage() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryTaskRepositoryFake()
        val viewModel = TasksViewModel(repository)
        advanceUntilIdle()

        viewModel.addTask("Prepare lab report", "Attach real screenshots", "HIGH")
        advanceUntilIdle()

        assertEquals(4, repository.count())
        assertEquals(UiMessage.TaskAdded, viewModel.uiMessage.value)
    }

    @Test fun deleteCompletedRemovesOnlyCompletedRows() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryTaskRepositoryFake()
        val viewModel = TasksViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteCompleted()
        advanceUntilIdle()

        assertEquals(2, repository.count())
        assertEquals(UiMessage.CompletedRemoved, viewModel.uiMessage.value)
    }

}

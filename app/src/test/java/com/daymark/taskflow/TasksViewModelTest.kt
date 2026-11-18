
package com.daymark.taskflow

import com.daymark.taskflow.BuildConfig
import com.daymark.taskflow.data.InMemoryTaskRepositoryFake
import com.daymark.taskflow.ui.TasksViewModel
import com.daymark.taskflow.ui.UiMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TasksViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seededTaskCount: Int
        get() = if (BuildConfig.BUILD_TYPE == "benchmark") 30 else 3

    @Test
    fun initialTasksAreSeededAndObservedThroughStateFlow() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = InMemoryTaskRepositoryFake()
            val viewModel = TasksViewModel(repository)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.tasks.collect { }
            }

            advanceUntilIdle()

            assertEquals(seededTaskCount, repository.count())
            assertEquals(seededTaskCount, viewModel.tasks.value.size)

            val expectedCompleted = if (BuildConfig.BUILD_TYPE == "benchmark") 0 else 1
            assertEquals(
                expectedCompleted,
                viewModel.tasks.value.count { it.isCompleted },
            )
        }

    @Test
    fun addTaskUpdatesRepositoryAndPublishesUiMessage() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = InMemoryTaskRepositoryFake()
            val viewModel = TasksViewModel(repository)

            advanceUntilIdle()

            viewModel.addTask(
                "Prepare lab report",
                "Attach real screenshots",
                "HIGH",
            )

            advanceUntilIdle()

            assertEquals(seededTaskCount + 1, repository.count())
            assertEquals(UiMessage.TaskAdded, viewModel.uiMessage.value)
        }

    @Test
    fun deleteCompletedRemovesOnlyCompletedRows() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = InMemoryTaskRepositoryFake()
            val viewModel = TasksViewModel(repository)

            advanceUntilIdle()

            viewModel.deleteCompleted()

            advanceUntilIdle()

            val expectedCount = if (BuildConfig.BUILD_TYPE == "benchmark") 30 else 2
            assertEquals(expectedCount, repository.count())
            assertEquals(
                UiMessage.CompletedRemoved,
                viewModel.uiMessage.value,
            )
        }
}

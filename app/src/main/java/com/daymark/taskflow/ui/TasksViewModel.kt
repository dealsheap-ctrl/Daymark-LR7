package com.daymark.taskflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daymark.taskflow.BuildConfig
import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.data.repository.TaskRepository
import com.daymark.taskflow.debug.AppLog
import com.daymark.taskflow.debug.AppOperationTracker
import com.daymark.taskflow.domain.AddTaskOutcome
import com.daymark.taskflow.domain.TaskInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiMessage {
    data object TaskAdded : UiMessage

    data object SyncStarted : UiMessage

    data object SyncFailed : UiMessage

    data class SyncComplete(
        val imported: Int,
    ) : UiMessage

    data object CompletedRemoved : UiMessage

    data class ValidationError(
        val message: String,
    ) : UiMessage
}

class TasksViewModel(
    private val repository: TaskRepository,
) : ViewModel() {
    private val interactor = TaskInteractor(repository)
    val tasks: StateFlow<List<Task>> =
        repository.observeTasks().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    init {
        launchTracked {
            if (repository.count() == 0) seedStarterTasks()
        }
    }

    fun addTask(
        title: String,
        note: String,
        priority: String,
    ) = launchTracked {
        AppLog.d("Creating task")
        when (val outcome = interactor.addTask(title, note, priority, System.currentTimeMillis() + 86_400_000L)) {
            is AddTaskOutcome.Saved -> _uiMessage.value = UiMessage.TaskAdded
            is AddTaskOutcome.Invalid -> _uiMessage.value = UiMessage.ValidationError(outcome.reason)
        }
    }

    fun setCompleted(
        id: Long,
        completed: Boolean,
    ) = launchTracked {
        interactor.toggleTask(id, completed)
    }

    fun deleteTask(id: Long) =
        launchTracked {
            interactor.deleteTask(id)
        }

    fun deleteCompleted() =
        launchTracked {
            interactor.deleteCompleted()
            _uiMessage.value = UiMessage.CompletedRemoved
        }

    fun syncRemote() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        launchTracked {
            _uiMessage.value = UiMessage.SyncStarted
            try {
                AppLog.i("Remote sync started")
                val imported = interactor.syncRemote()
                AppLog.i("Remote sync synchronized $imported tasks")
                _uiMessage.value = UiMessage.SyncComplete(imported)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                AppLog.w("Remote sync failed", error)
                _uiMessage.value = UiMessage.SyncFailed
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /** Counts every asynchronous app operation, not only network Sync, for deterministic Espresso waits. */
    private fun launchTracked(block: suspend () -> Unit) {
        viewModelScope.launch {
            AppOperationTracker.increment()
            try {
                block()
            } finally {
                AppOperationTracker.decrement()
            }
        }
    }

    private suspend fun seedStarterTasks() {
        val now = System.currentTimeMillis()
        if (BuildConfig.BUILD_TYPE == "benchmark") {
            // A longer deterministic list is created only in the non-debuggable
            // benchmark variant so scroll benchmarks exercise RecyclerView virtualization.
            repeat(30) { index ->
                repository.add(
                    Task(
                        title = "Benchmark task ${index + 1}",
                        note = "Synthetic row for scroll frame timing",
                        priority = if (index % 3 == 0) "HIGH" else "NORMAL",
                        dueAt = now + (index + 1) * 3_600_000L,
                    ),
                )
            }
            return
        }
        repository.add(
            Task(
                title = "Plan the week",
                note = "Choose three things that matter most",
                priority = "HIGH",
                dueAt =
                    now + 3_600_000L,
            ),
        )
        repository.add(
            Task(
                title = "Clear the small tasks",
                note = "A quick win for a calmer day",
                priority = "NORMAL",
                dueAt =
                    now + 21_600_000L,
            ),
        )
        repository.add(
            Task(
                title = "Make time to recharge",
                note = "A short walk counts",
                priority = "LOW",
                dueAt =
                    now + 86_400_000L,
                isCompleted = true,
            ),
        )
    }

    class Factory(
        private val repository: TaskRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TasksViewModel::class.java))
            return TasksViewModel(repository) as T
        }
    }
}

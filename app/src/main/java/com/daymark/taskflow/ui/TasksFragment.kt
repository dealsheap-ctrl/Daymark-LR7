package com.daymark.taskflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.daymark.taskflow.R
import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.databinding.FragmentTasksBinding
import com.daymark.taskflow.domain.TaskMetrics
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskAdapter
    private var allTasks: List<Task> = emptyList()
    private var selectedFilter = Filter.ALL

    private val viewModel: TasksViewModel
        get() = (requireActivity() as MainActivity).tasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedFilter = savedInstanceState?.getString(STATE_FILTER)?.let { saved ->
            runCatching { Filter.valueOf(saved) }.getOrNull()
        } ?: Filter.ALL
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_FILTER, selectedFilter.name)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        adapter =
            TaskAdapter(
                onToggle = { task, completed -> viewModel.setCompleted(task.id, completed) },
                onDelete = { task -> viewModel.deleteTask(task.id) },
            )
        binding.recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTasks.adapter = adapter
        binding.fabAddTask.setOnClickListener {
            AddTaskDialogFragment().show(
                parentFragmentManager,
                AddTaskDialogFragment.TAG,
            )
        }
        binding.btnSync.setOnClickListener { viewModel.syncRemote() }
        binding.chipAll.setOnClickListener {
            selectedFilter = Filter.ALL
            renderTasks()
        }
        binding.chipOpen.setOnClickListener {
            selectedFilter = Filter.OPEN
            renderTasks()
        }
        binding.chipDone.setOnClickListener {
            selectedFilter = Filter.DONE
            renderTasks()
        }
        binding.chipFilters.check(
            when (selectedFilter) {
                Filter.ALL -> R.id.chipAll
                Filter.OPEN -> R.id.chipOpen
                Filter.DONE -> R.id.chipDone
            },
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasks.collect { tasks ->
                        allTasks = tasks
                        renderTasks()
                        renderProgress(tasks)
                    }
                }
                launch {
                    viewModel.uiMessage.collect { message ->
                        binding.tvSyncStatus.text =
                            when (message) {
                                UiMessage.SyncStarted -> getString(R.string.sync_status_loading)
                                UiMessage.SyncFailed -> getString(R.string.sync_unavailable)
                                is UiMessage.SyncComplete -> getString(R.string.sync_complete, message.imported)
                                UiMessage.TaskAdded -> getString(R.string.task_added)
                                UiMessage.CompletedRemoved -> getString(R.string.completed_removed)
                                is UiMessage.ValidationError -> message.message
                                null -> ""
                            }
                        if (message ==
                            UiMessage.TaskAdded
                        ) {
                            Snackbar.make(binding.root, R.string.task_added_snackbar, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    viewModel.isSyncing.collect { syncing ->
                        binding.btnSync.isEnabled = !syncing
                        binding.btnSync.text =
                            getString(if (syncing) R.string.syncing else R.string.sync)
                    }
                }
            }
        }
    }

    private fun renderTasks() {
        val visible =
            when (selectedFilter) {
                Filter.ALL -> allTasks
                Filter.OPEN -> allTasks.filterNot { it.isCompleted }
                Filter.DONE -> allTasks.filter { it.isCompleted }
            }
        adapter.submitList(visible)
        val isEmpty = visible.isEmpty()
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.tvEmptyTitle.text =
            getString(if (selectedFilter == Filter.ALL) R.string.empty_tasks_title else R.string.empty_filter_title)
        binding.tvEmptySubtitle.text =
            getString(
                if (selectedFilter ==
                    Filter.ALL
                ) {
                    R.string.empty_tasks_subtitle
                } else {
                    R.string.empty_filter_subtitle
                },
            )
    }

    private fun renderProgress(tasks: List<Task>) {
        val completed = TaskMetrics.completedCount(tasks)
        val percent = TaskMetrics.completionPercent(completed, tasks.size)
        binding.tvCompletionPercent.text = "$percent%"
        binding.tvProgressSummary.text =
            when {
                tasks.isEmpty() -> getString(R.string.momentum_empty)
                completed == tasks.size -> getString(R.string.momentum_complete)
                else -> getString(R.string.insight_count_caption, completed, tasks.size)
            }
        binding.progressToday.setProgressCompat(percent, true)
    }

    override fun onDestroyView() {
        binding.recyclerTasks.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private enum class Filter { ALL, OPEN, DONE }

    companion object {
        private const val STATE_FILTER = "selected_filter"
    }
}

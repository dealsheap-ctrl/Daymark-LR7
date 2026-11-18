package com.daymark.taskflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.daymark.taskflow.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.daymark.taskflow.databinding.FragmentInsightsBinding
import com.daymark.taskflow.domain.TaskMetrics
import kotlinx.coroutines.launch

class InsightsFragment : Fragment() {
    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TasksViewModel get() = (requireActivity() as MainActivity).tasksViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    val done = TaskMetrics.completedCount(tasks)
                    val open = TaskMetrics.openCount(tasks)
                    val percent = TaskMetrics.completionPercent(done, tasks.size)
                    binding.tvInsightPercent.text = "$percent%"
                    binding.progressInsights.setProgressCompat(percent, true)
                    binding.tvOpenCount.text = open.toString()
                    binding.tvDoneCount.text = done.toString()
                    binding.tvInsightCaption.text = getString(R.string.insight_count_caption, done, tasks.size)
                    binding.tvInsightTip.text = if (open == 0 && tasks.isNotEmpty()) getString(R.string.insight_everything_done) else getString(R.string.insight_focus_tip)
                }
            }
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}

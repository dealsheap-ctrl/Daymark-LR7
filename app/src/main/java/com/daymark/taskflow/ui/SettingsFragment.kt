package com.daymark.taskflow.ui

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.daymark.taskflow.BuildConfig
import com.daymark.taskflow.R
import com.daymark.taskflow.databinding.FragmentSettingsBinding
import com.daymark.taskflow.debug.DebugCrashScenarios
import com.daymark.taskflow.debug.DebugLeakScenarios
import com.daymark.taskflow.debug.AppLog

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TasksViewModel get() = (requireActivity() as MainActivity).tasksViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnShareApp.setOnClickListener {
            val intent = ShareIntentFactory.create(getString(R.string.share_message))
            try { startActivity(android.content.Intent.createChooser(intent, getString(R.string.share_chooser_title))) }
            catch (_: ActivityNotFoundException) { Toast.makeText(requireContext(), R.string.no_share_app, Toast.LENGTH_SHORT).show() }
        }
        binding.btnDeleteCompleted.setOnClickListener {
            viewModel.deleteCompleted()
            Toast.makeText(requireContext(), R.string.completed_removed, Toast.LENGTH_SHORT).show()
        }
        if (BuildConfig.ENABLE_DIAGNOSTICS) {
            binding.debugPanel.visibility = View.VISIBLE
            binding.btnCrashNpe.setOnClickListener {
                runDebugScenario(R.string.crash_npe) { DebugCrashScenarios.nullPointer() }
            }
            binding.btnCrashIndex.setOnClickListener {
                runDebugScenario(R.string.crash_index) { DebugCrashScenarios.indexOutOfBounds() }
            }
            binding.btnCrashNetwork.setOnClickListener {
                runDebugScenario(R.string.crash_network) { DebugCrashScenarios.networkOnMainThread() }
            }
            binding.btnWriteLogSamples.setOnClickListener {
                AppLog.d("Sample debug entry")
                AppLog.i("Sample info entry")
                AppLog.w("Sample warning entry")
                AppLog.e("Sample error entry", IllegalStateException("Intentional Logcat sample; no crash"))
                Toast.makeText(requireContext(), R.string.log_samples_written, Toast.LENGTH_SHORT).show()
            }
            binding.btnSimulateLeak.setOnClickListener {
                DebugLeakScenarios.retain(requireActivity())
                Toast.makeText(requireContext(), R.string.leak_retained, Toast.LENGTH_LONG).show()
            }
            binding.btnReleaseLeak.setOnClickListener {
                DebugLeakScenarios.release()
                Toast.makeText(requireContext(), R.string.leak_released, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * The lab deliberately throws platform exceptions, but a diagnostic button must not
     * terminate the whole application. The exception breakpoint can still pause here
     * when Android Studio is configured to break on caught exceptions.
     */
    private fun runDebugScenario(labelRes: Int, action: () -> Unit) {
        try {
            action()
        } catch (exception: Exception) {
            val label = getString(labelRes)
            AppLog.e("Caught expected diagnostic exception: $label", exception)
            Toast.makeText(
                requireContext(),
                getString(R.string.debug_exception_caught, exception.javaClass.simpleName),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}

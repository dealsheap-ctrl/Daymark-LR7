package com.daymark.taskflow.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.daymark.taskflow.R
import com.daymark.taskflow.databinding.DialogAddTaskBinding
import com.daymark.taskflow.domain.TaskValidator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AddTaskDialogFragment : DialogFragment() {
    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddTaskBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_task_title)
            .setView(binding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save_task, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                id = R.id.btnSaveTask
                setOnClickListener {
                val validation = TaskValidator.validateTitle(binding.inputTaskTitle.text?.toString().orEmpty())
                if (!validation.isValid) {
                    binding.tilTaskTitle.error = if (binding.inputTaskTitle.text?.toString().orEmpty().isBlank()) getString(R.string.validation_title_required) else getString(R.string.validation_title_too_long)
                    binding.inputTaskTitle.requestFocus()
                    return@setOnClickListener
                }
                binding.tilTaskTitle.error = null
                val priority = when (binding.priorityGroup.checkedChipId) {
                    R.id.chipPriorityLow -> "LOW"
                    R.id.chipPriorityHigh -> "HIGH"
                    else -> "NORMAL"
                }
                (requireActivity() as MainActivity).tasksViewModel.addTask(
                    validation.normalizedValue,
                    binding.inputTaskNote.text?.toString().orEmpty(),
                    priority
                )
                dismiss()
                }
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object { const val TAG = "AddTaskDialog" }
}

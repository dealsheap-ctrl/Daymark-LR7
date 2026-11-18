package com.daymark.taskflow.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.daymark.taskflow.R
import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onToggle: (Task, Boolean) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) = holder.bind(getItem(position))

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskTitle.paint.isStrikeThruText = task.isCompleted
            binding.tvTaskNote.text = task.note.ifBlank { binding.root.context.getString(R.string.task_note_fallback) }
            val day = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(task.dueAt))
            val priorityLabel = when (task.priority) {
                "LOW" -> binding.root.context.getString(R.string.priority_low)
                "HIGH" -> binding.root.context.getString(R.string.priority_high)
                else -> binding.root.context.getString(R.string.priority_normal)
            }
            binding.tvTaskMeta.text = binding.root.context.getString(R.string.task_meta, day.uppercase(Locale.getDefault()), priorityLabel)
            binding.checkboxCompleted.setOnCheckedChangeListener(null)
            binding.checkboxCompleted.isChecked = task.isCompleted
            binding.checkboxCompleted.setOnCheckedChangeListener { _, checked -> onToggle(task, checked) }
            binding.btnDeleteTask.setOnClickListener { onDelete(task) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
        }
    }
}

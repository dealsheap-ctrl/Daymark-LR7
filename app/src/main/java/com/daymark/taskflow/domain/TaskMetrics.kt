package com.daymark.taskflow.domain

import com.daymark.taskflow.data.local.Task

object TaskMetrics {
    fun completionPercent(
        completed: Int,
        total: Int,
    ): Int {
        if (total <= 0) return 0
        val rawPercent = ((completed.toDouble() / total.toDouble()) * 100.0).toInt()
        val percent = rawPercent.coerceIn(0, 100)
        return percent
    }

    fun openCount(tasks: List<Task>): Int = tasks.count { !it.isCompleted }

    fun completedCount(tasks: List<Task>): Int = tasks.count { it.isCompleted }

    fun overdueCount(
        tasks: List<Task>,
        nowMillis: Long,
    ): Int = tasks.count { !it.isCompleted && it.dueAt < nowMillis }
}

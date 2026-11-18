package com.daymark.taskflow.domain

import com.daymark.taskflow.data.local.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDomainTest {
    @Test fun acceptsNormalTitle() {
        val result = TaskValidator.validateTitle("Prepare presentation")
        assertTrue(result.isValid)
        assertEquals("Prepare presentation", result.normalizedValue)
    }

    @Test fun trimsAndCollapsesWhitespace() {
        val result = TaskValidator.validateTitle("  Read   two   pages  ")
        assertTrue(result.isValid)
        assertEquals("Read two pages", result.normalizedValue)
    }

    @Test fun rejectsBlankTitle() {
        val result = TaskValidator.validateTitle("  \n  ")
        assertFalse(result.isValid)
        assertEquals("Enter a task name", result.message)
    }

    @Test fun acceptsTitleAtMaximumLength() {
        assertTrue(TaskValidator.validateTitle("x".repeat(80)).isValid)
    }

    @Test fun rejectsTitleLongerThanMaximum() {
        val result = TaskValidator.validateTitle("x".repeat(81))
        assertFalse(result.isValid)
        assertEquals("Use 80 characters or fewer", result.message)
    }

    @Test fun noteIsTrimmedAndLimited() {
        assertEquals("short note", TaskValidator.normalizeNote("  short note  "))
        assertEquals(500, TaskValidator.normalizeNote("x".repeat(600)).length)
    }

    @Test fun completionIsZeroForEmptyList() {
        assertEquals(0, TaskMetrics.completionPercent(0, 0))
    }

    @Test fun completionIsZeroWhenNothingIsDone() {
        assertEquals(0, TaskMetrics.completionPercent(0, 4))
    }

    @Test fun completionUsesIntegerPercentage() {
        assertEquals(33, TaskMetrics.completionPercent(1, 3))
    }

    @Test fun completionIsOneHundredWhenAllDone() {
        assertEquals(100, TaskMetrics.completionPercent(5, 5))
    }

    @Test fun completionIsClampedToValidRange() {
        assertEquals(100, TaskMetrics.completionPercent(6, 5))
        assertEquals(0, TaskMetrics.completionPercent(-1, 5))
    }

    @Test fun metricsCountOpenAndCompletedTasks() {
        val tasks =
            listOf(
                Task(id = 1, title = "A", isCompleted = false),
                Task(id = 2, title = "B", isCompleted = true),
                Task(id = 3, title = "C", isCompleted = false),
            )
        assertEquals(2, TaskMetrics.openCount(tasks))
        assertEquals(1, TaskMetrics.completedCount(tasks))
    }

    @Test fun overdueExcludesCompletedTasks() {
        val now = 10_000L
        val tasks =
            listOf(
                Task(id = 1, title = "Late", dueAt = now - 1),
                Task(id = 2, title = "Done late", dueAt = now - 2, isCompleted = true),
                Task(id = 3, title = "Future", dueAt = now + 1),
            )
        assertEquals(1, TaskMetrics.overdueCount(tasks, now))
    }
}

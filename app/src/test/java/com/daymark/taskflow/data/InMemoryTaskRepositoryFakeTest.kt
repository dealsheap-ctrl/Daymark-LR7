package com.daymark.taskflow.data

import com.daymark.taskflow.data.local.Task
import com.daymark.taskflow.domain.AddTaskOutcome
import com.daymark.taskflow.domain.TaskInteractor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryTaskRepositoryFakeTest {
    @Test fun fakeProvidesWorkingInMemoryBehavior() =
        runTest {
            val fake = InMemoryTaskRepositoryFake()
            val interactor = TaskInteractor(fake)

            val outcome = interactor.addTask("Read a chapter", "Ten pages", "NORMAL", 123L)
            assertEquals(AddTaskOutcome.Saved(1L), outcome)
            assertEquals(
                "Read a chapter",
                fake
                    .observeTasks()
                    .first()
                    .single()
                    .title,
            )

            interactor.toggleTask(1L, true)
            assertTrue(
                fake
                    .observeTasks()
                    .first()
                    .single()
                    .isCompleted,
            )
            assertEquals(1, fake.count())
        }
}

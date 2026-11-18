package com.daymark.taskflow.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TaskValidatorParameterizedTest(
    private val input: String,
    private val expectedValid: Boolean,
    private val expectedNormalized: String
) {
    @Test fun validatesBoundaryAndNormalizationCases() {
        val result = TaskValidator.validateTitle(input)
        assertEquals(expectedValid, result.isValid)
        assertEquals(expectedNormalized, result.normalizedValue)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "case {index}: valid={1}")
        fun inputs(): Collection<Array<Any>> = listOf(
            arrayOf<Any>("Plan the week", true, "Plan the week"),
            arrayOf<Any>("  Read   a book  ", true, "Read a book"),
            arrayOf<Any>("  \n  ", false, ""),
            arrayOf<Any>("x".repeat(80), true, "x".repeat(80)),
            arrayOf<Any>("x".repeat(81), false, "x".repeat(81))
        )
    }
}

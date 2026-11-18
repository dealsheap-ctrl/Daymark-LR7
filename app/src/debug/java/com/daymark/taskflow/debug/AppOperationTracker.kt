package com.daymark.taskflow.debug

import androidx.test.espresso.idling.CountingIdlingResource

/** Tracks Room and network work so Espresso waits for stable UI state. */
object AppOperationTracker {
    @JvmField
    val resource = CountingIdlingResource("daymark-background-operations")

    fun increment() = resource.increment()

    fun decrement() {
        if (!resource.isIdleNow) resource.decrement()
    }
}

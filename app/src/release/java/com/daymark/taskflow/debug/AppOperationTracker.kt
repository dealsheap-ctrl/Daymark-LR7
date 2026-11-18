package com.daymark.taskflow.debug

/** No-op in release: the Espresso dependency and its tracking code are debug-only. */
object AppOperationTracker {
    fun increment() = Unit

    fun decrement() = Unit
}

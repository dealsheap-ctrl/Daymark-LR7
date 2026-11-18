package com.daymark.taskflow.debug

/** Release-safe no-op implementation. Diagnostic exception triggers exist only in debug. */
object DebugCrashScenarios {
    fun nullPointer() = Unit
    fun indexOutOfBounds() = Unit
    fun networkOnMainThread() = Unit
}

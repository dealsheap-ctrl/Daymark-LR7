package com.daymark.taskflow.debug

import android.app.Activity

/** Release-safe no-op; leak reproduction is available only in debug builds. */
object DebugLeakScenarios {
    fun retain(activity: Activity) = Unit

    fun release() = Unit
}

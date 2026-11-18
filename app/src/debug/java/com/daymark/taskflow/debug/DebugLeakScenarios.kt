
package com.daymark.taskflow.debug

import android.annotation.SuppressLint
import android.app.Activity

object DebugLeakScenarios {
    @Volatile
    @SuppressLint("StaticFieldLeak")
    private var retainedActivity: Activity? = null

    fun retain(activity: Activity) {
        retainedActivity = activity
        AppLog.w("Memory leak simulation: Activity retained: ${activity.javaClass.simpleName}")
    }

    fun release() {
        val wasRetained = retainedActivity != null
        retainedActivity = null

        AppLog.i("Memory leak simulation: retained Activity released = $wasRetained")
    }
}

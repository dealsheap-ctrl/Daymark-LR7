package com.daymark.taskflow.debug

import android.util.Log
import com.daymark.taskflow.BuildConfig

/** All diagnostic logging is compiled out of behavior in release builds. */
object AppLog {
    private const val TAG = "Daymark"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    fun w(
        message: String,
        error: Throwable? = null,
    ) {
        if (BuildConfig.DEBUG) Log.w(TAG, message, error)
    }

    fun e(
        message: String,
        error: Throwable? = null,
    ) {
        if (BuildConfig.DEBUG) Log.e(TAG, message, error)
    }
}

package com.daymark.taskflow.debug

import android.os.Looper
import android.os.NetworkOnMainThreadException
import android.os.StrictMode
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Deliberate exception reproductions for the lab. SettingsFragment catches these exceptions,
 * logs their complete stack traces and keeps the app alive. This object is only in debug.
 */
object DebugCrashScenarios {
    fun nullPointer() {
        val value: String? = null
        value!!.length
    }

    fun indexOutOfBounds() {
        val values = listOf("only-item")
        values[2]
    }

    fun networkOnMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "Run this diagnostic from the UI thread"
        }

        val previousPolicy = StrictMode.getThreadPolicy()
        // penaltyLog records the violation. penaltyDeath is intentionally NOT used because
        // it kills the entire process before the UI can handle the diagnostic exception.
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy
                .Builder()
                .detectNetwork()
                .penaltyLog()
                .build(),
        )
        try {
            // A loopback-only request exercises main-thread network I/O without contacting
            // an external host. Port 9 is normally closed; connection failure is expected.
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress("127.0.0.1", 9), 250)
            } catch (_: IOException) {
                // The local connection is only used to trigger StrictMode's network detector.
            } finally {
                runCatching { socket.close() }
            }

            // Throw the exact Android exception deterministically so the debugger can inspect it.
            throw NetworkOnMainThreadException()
        } finally {
            StrictMode.setThreadPolicy(previousPolicy)
        }
    }
}

package com.daymark.taskflow

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** Starts the target APK with DaymarkTestApplication so Espresso tests never call a live service. */
class DaymarkTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader,
        className: String?,
        context: Context,
    ): Application =
        super.newApplication(
            classLoader,
            DaymarkTestApplication::class.java.name,
            context,
        )
}


package com.daymark.taskflow.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val packageName = "com.daymark.taskflow"

    @Test
    fun coldStart() = benchmarkRule.measureRepeated(
        packageName = packageName,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.None(),
        startupMode = StartupMode.COLD,
        iterations = 5,
        setupBlock = {
            pressHome()
        }
    ) {
        startActivityAndWait()
    }

    @Test
    fun taskListScrollFrameTiming() {
        val device = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation()
        )

        benchmarkRule.measureRepeated(
            packageName = packageName,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.None(),
            iterations = 5,
            setupBlock = {
                startActivityAndWait()
                device.waitForIdle()

                val screenReady = device.wait(
                    Until.hasObject(
                        By.res(packageName, "recyclerTasks")
                    ),
                    5000
                )

                check(screenReady) {
                    "Task list screen did not appear"
                }
            }
        ) {
            val list = requireNotNull(
                device.findObject(
                    By.res(packageName, "recyclerTasks")
                )
            ) {
                "Task list RecyclerView was not found"
            }

            list.fling(Direction.DOWN)
            list.fling(Direction.UP)
        }
    }
}

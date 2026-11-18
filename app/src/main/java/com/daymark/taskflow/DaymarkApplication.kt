package com.daymark.taskflow

import android.app.Application
import android.os.StrictMode
import androidx.room.Room
import com.daymark.taskflow.data.local.TaskDatabase
import com.daymark.taskflow.data.remote.HttpTaskApi
import com.daymark.taskflow.data.repository.DefaultTaskRepository
import com.daymark.taskflow.data.repository.TaskRepository

open class DaymarkApplication : Application() {
    lateinit var database: TaskDatabase
        private set
    lateinit var repository: TaskRepository
        private set

    protected open fun createRemoteApi(): com.daymark.taskflow.data.remote.RemoteTaskApi = HttpTaskApi()

    override fun onCreate() {
        super.onCreate()
        database =
            Room
                .databaseBuilder(this, TaskDatabase::class.java, "daymark.db")
                .addMigrations(TaskDatabase.MIGRATION_1_2, TaskDatabase.MIGRATION_2_3)
                .build()
        repository = DefaultTaskRepository(database.taskDao(), createRemoteApi())

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy
                    .Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build(),
            )
        }
    }
}

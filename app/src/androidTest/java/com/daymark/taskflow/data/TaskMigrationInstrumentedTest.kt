package com.daymark.taskflow.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.daymark.taskflow.data.local.TaskDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskMigrationInstrumentedTest {
    @Test fun versionOneDatabaseMigratesAndKeepsExistingRows() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "daymark_migration_test.db"
        context.deleteDatabase(databaseName)
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        val oldDb = SQLiteDatabase.openOrCreateDatabase(file, null)
        oldDb.execSQL("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, note TEXT NOT NULL, priority TEXT NOT NULL, dueAt INTEGER NOT NULL, isCompleted INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
        oldDb.execSQL("INSERT INTO tasks (title, note, priority, dueAt, isCompleted, createdAt) VALUES ('Legacy task', '', 'NORMAL', 100, 0, 90)")
        oldDb.version = 1
        oldDb.close()

        val migrated = Room.databaseBuilder(context, TaskDatabase::class.java, databaseName)
            .addMigrations(TaskDatabase.MIGRATION_1_2, TaskDatabase.MIGRATION_2_3)
            .build()
        try {
            val row = migrated.taskDao().observeAll().first().single()
            assertEquals("Legacy task", row.title)
            assertEquals(false, row.isPinned)
            assertEquals(null, row.remoteId)
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
    }
}

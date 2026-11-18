package com.daymark.taskflow.ui

import android.content.Intent

object ShareIntentFactory {
    fun create(message: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
}

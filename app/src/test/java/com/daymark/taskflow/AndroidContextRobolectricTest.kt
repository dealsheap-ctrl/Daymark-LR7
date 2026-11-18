package com.daymark.taskflow

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.daymark.taskflow.data.local.PreferenceStore
import com.daymark.taskflow.ui.ShareIntentFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidContextRobolectricTest {
    @Test fun selectedTabPersistsInSharedPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = PreferenceStore(context)
        preferences.saveSelectedTab(1234)
        assertEquals(1234, preferences.selectedTab(0))
    }

    @Test fun appNameResourceIsAvailableWithoutEmulator() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Daymark", context.getString(R.string.app_name))
    }

    @Test fun shareIntentContainsPlainTextPayload() {
        val intent = ShareIntentFactory.create("A calmer way to plan")
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("A calmer way to plan", intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}

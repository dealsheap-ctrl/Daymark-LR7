
package com.daymark.taskflow.data.local

import android.content.Context

class PreferenceStore(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("daymark_preferences", Context.MODE_PRIVATE)

    fun saveSelectedTab(tabId: Int) {
        preferences.edit().putInt(KEY_SELECTED_TAB, tabId).apply()
    }

    fun selectedTab(defaultValue: Int): Int {
        return preferences.getInt(KEY_SELECTED_TAB, defaultValue)
    }

    companion object {
        private const val KEY_SELECTED_TAB = "selected_tab"
    }
}

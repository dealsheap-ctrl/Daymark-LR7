
package com.daymark.taskflow.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.daymark.taskflow.DaymarkApplication
import com.daymark.taskflow.R
import com.daymark.taskflow.data.local.PreferenceStore
import com.daymark.taskflow.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var tasksViewModel: TasksViewModel
        private set
    private lateinit var preferences: PreferenceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as DaymarkApplication
        tasksViewModel =
            ViewModelProvider(
                this,
                TasksViewModel.Factory(app.repository),
            )[TasksViewModel::class.java]

        preferences = PreferenceStore(this)

        binding.btnHeaderAdd.setOnClickListener {
            AddTaskDialogFragment().show(
                supportFragmentManager,
                AddTaskDialogFragment.TAG,
            )
        }

        lifecycleScope.launch {
            val selected =
                withContext(Dispatchers.IO) {
                    preferences.selectedTab(R.id.navigation_tasks)
                }

            binding.bottomNavigation.selectedItemId = selected

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                preferences.saveSelectedTab(item.itemId)
                showScreen(item.itemId)
                true
            }

            showScreen(selected)
        }
    }

    private fun showScreen(itemId: Int) {
        val currentTag =
            when (itemId) {
                R.id.navigation_insights -> "insights"
                R.id.navigation_settings -> "settings"
                else -> "tasks"
            }

        val currentlyVisible =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        if (currentlyVisible?.tag == currentTag) return

        val fragment =
            when (itemId) {
                R.id.navigation_insights -> InsightsFragment()
                R.id.navigation_settings -> SettingsFragment()
                else -> TasksFragment()
            }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, currentTag)
            .commit()
    }
}

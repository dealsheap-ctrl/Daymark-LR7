package com.daymark.taskflow.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.daymark.taskflow.R

/** Page Object keeps UI selectors and actions out of the scenario itself. */
class TasksPageObject {
    fun addTask(
        title: String,
        note: String = "",
    ) {
        onView(withId(R.id.fabAddTask)).perform(click())
        onView(withId(R.id.inputTaskTitle)).perform(replaceText(title), closeSoftKeyboard())
        if (note.isNotEmpty()) onView(withId(R.id.inputTaskNote)).perform(replaceText(note), closeSoftKeyboard())
        onView(withId(R.id.btnSaveTask)).perform(click())
    }

    fun assertTaskVisible(title: String) {
        onView(withId(R.id.recyclerTasks)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(title))),
        )
        onView(withText(title)).check(matches(isDisplayed()))
    }

    fun openInsights() {
        onView(withId(R.id.navigation_insights)).perform(click())
    }

    fun openSettings() {
        onView(withId(R.id.navigation_settings)).perform(click())
    }
}

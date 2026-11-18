package com.daymark.taskflow.ui

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.daymark.taskflow.R
import com.daymark.taskflow.debug.AppOperationTracker
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.isEmptyOrNullString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskFlowUiTest {
    @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private val page = TasksPageObject()

    @Before fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(AppOperationTracker.resource)
    }

    @After fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(AppOperationTracker.resource)
    }

    @Test fun createTaskAndFindItOnTheList() {
        page.addTask("Espresso acceptance task", "Created from the form")
        page.assertTaskVisible("Espresso acceptance task")
    }

    @Test fun navigationOpensInsightsScreen() {
        page.openInsights()
        onView(withId(R.id.tvInsightPercent)).check(matches(isDisplayed()))
    }

    @Test fun syncFinishesAndUpdatesStatusUsingIdlingResource() {
        onView(withId(R.id.btnSync)).perform(androidx.test.espresso.action.ViewActions.click())
        onView(withId(R.id.tvSyncStatus)).check(matches(withText(not(isEmptyOrNullString()))))
        page.assertTaskVisible("UI test imported task")
    }

    @Test fun blankTitleIsRejectedByTheForm() {
        onView(withId(R.id.fabAddTask)).perform(click())
        onView(withId(R.id.inputTaskTitle)).perform(replaceText("   "))
        onView(withId(R.id.btnSaveTask)).perform(click())
        onView(withId(R.id.tilTaskTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.inputTaskTitle)).check(matches(isDisplayed()))
    }

    @Test fun doneFilterShowsCompletedSeedTask() {
        onView(withId(R.id.chipDone)).perform(click())
        onView(withText("Make time to recharge")).check(matches(isDisplayed()))
    }

    @Test fun shareButtonLaunchesChooserIntent() {
        Intents.init()
        try {
            page.openSettings()
            onView(withId(R.id.btnShareApp)).perform(androidx.test.espresso.action.ViewActions.click())
            intended(hasAction(Intent.ACTION_CHOOSER))
        } finally {
            Intents.release()
        }
    }
}

package it.filo.maggioliebook.android.screengrab

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import it.filo.maggioliebook.android.BaseKoinScreengrabTest
import it.filo.maggioliebook.android.R
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab

@RunWith(JUnit4::class)
class PhoneScreenshotTest: BaseKoinScreengrabTest() {

    @Test
    fun takeLoginScreenshotTest() {
        Espresso.onView(ViewMatchers.withId(R.id.username))
            .perform(ViewActions.replaceText("username@maggioli.it"))
        Espresso.onView(ViewMatchers.withId(R.id.password))
            .perform(ViewActions.replaceText("username@maggioli.it"))
        Screengrab.screenshot(System.getProperty("ADV","screenshot").plus("_login"))
    }

    @Test
    fun takeHomePageScreenshotTest() {
        Espresso.onView(ViewMatchers.withId(R.id.username))
            .perform(ViewActions.replaceText("admin"))
        Espresso.onView(ViewMatchers.withId(R.id.password))
            .perform(ViewActions.replaceText("admin"))
        Espresso.onView(ViewMatchers.withId(R.id.login))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.home_search_view))
            .perform(ViewActions.click())

        Screengrab.screenshot(System.getProperty("ADV","screenshot").plus("_home"))
    }

    @Test
    fun takeUserScreenshotTest() {
        Espresso.onView(ViewMatchers.withId(R.id.login)).perform(ViewActions.click())
        Screengrab.screenshot(System.getProperty("ADV","screenshot").plus("_user"))
    }

    @Test
    fun takeSettingsScreenshotTest() {
        Espresso.onView(ViewMatchers.withId(R.id.username))
            .perform(ViewActions.replaceText("username@maggioli.it"))
        Espresso.onView(ViewMatchers.withId(R.id.password))
            .perform(ViewActions.replaceText("username@maggioli.it"))
        Screengrab.screenshot(System.getProperty("ADV","test_screenshot"))

        Screengrab.screenshot(System.getProperty("ADV","screenshot").plus("_settings"))
    }
}
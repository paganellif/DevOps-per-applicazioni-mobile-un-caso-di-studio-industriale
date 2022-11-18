package it.filo.maggioliebook.android

import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.After
import org.junit.Rule
import org.koin.core.context.stopKoin
import tools.fastlane.screengrab.locale.LocaleTestRule

open class BaseKoinTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @After
    fun tearDown() {
        stopKoin()
    }
}

open class BaseKoinScreengrabTest: BaseKoinTest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

}
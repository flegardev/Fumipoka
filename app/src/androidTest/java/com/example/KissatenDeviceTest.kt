package com.example

import android.os.Build
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

/** UI-only regression: no resets, seeding, imports, uninstalls or user-record deletion. */
@RunWith(AndroidJUnit4::class)
class KissatenDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun requireEmulator() {
        check(Build.MODEL.contains("sdk", ignoreCase=true)) { "Disposable Android emulator required" }
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        Thread.sleep(600)
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val folder=File(instrumentation.targetContext.getExternalFilesDir(null), "kissaten-qa")
        folder.mkdirs()
        val bitmap=checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(folder,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }
    @Test fun navigationAndThemeSurviveActivityRecreation() {
        for (label in listOf("Home","Projects","Save Points","Focus","Library")) {
            compose.onAllNodesWithText(label).onLast().performClick()
            compose.waitForIdle()
            capture(label.lowercase().replace(" ","-"))
        }
        compose.onAllNodesWithText("Home").onLast().performClick()
        for (mode in listOf("dark","oled","light","system")) {
            compose.onNodeWithTag("quick_appearance_$mode").performScrollTo().performClick()
            compose.waitUntil(15_000) {
                kotlinx.coroutines.runBlocking {
                    com.example.data.PreferencesRepository(compose.activity).getSnapshot().appearanceMode.name.equals(mode, true)
                }
            }
            compose.waitForIdle()
            Thread.sleep(800)
            capture("appearance-$mode")
        }
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.onAllNodesWithText("Home").onLast().assertIsDisplayed()
        capture("recreated")
    }
}

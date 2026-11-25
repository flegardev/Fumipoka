package com.example

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class NotebookDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun createProjectByNameAndSaveProgressWithOnlyANote() {
        assumeTrue(Build.MODEL.contains("sdk", ignoreCase = true))
        compose.onNodeWithText("New project").performClick()
        compose.onNodeWithText("Name").performTextInput("Tile journal")
        compose.onNodeWithText("Create project").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Tile journal").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Save progress").performClick()
        compose.onNodeWithTag("save_point_context_input").performTextInput("Test the glaze on the next tile")
        compose.onNodeWithTag("submit_save_point_button").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Test the glaze on the next tile").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Continue project").assertIsDisplayed()
    }
}

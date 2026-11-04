// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.farcaster.pixelbridge.ble.ProbeReport
import net.farcaster.pixelbridge.ble.ProbeStateHolder
import net.farcaster.pixelbridge.data.MusicStore
import net.farcaster.pixelbridge.data.NotificationActions
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicScreenTest {
    @get:Rule val compose = createComposeRule()
    private val commands = mutableListOf<String>()

    @Before
    fun setup() {
        MusicStore.clear()
        MusicStore.onInfo("Artist", "Album", "Test track", 200_000)
        MusicStore.onState("play", 0)
        ProbeStateHolder.update { ProbeReport(centralConnected = true, notifySubscribed = true) }
        NotificationActions.musicHandler = { commands.add(it); true }
    }

    @After
    fun cleanup() {
        NotificationActions.musicHandler = null
        MusicStore.clear()
        ProbeStateHolder.update { ProbeReport() }
    }

    @Test
    fun transportAndVolumeButtonsSendTheExpectedCommands() {
        compose.setContent { MusicScreen() }
        for (label in listOf("Pause", "Next track", "Previous track", "Volume down", "Volume up")) {
            compose.onNodeWithContentDescription(label).performScrollTo().performClick()
        }
        assertEquals(listOf("pause", "next", "previous", "volumedown", "volumeup"), commands)
    }

    @Test
    fun disconnectedPhoneDisablesTransportAndExplainsWhy() {
        ProbeStateHolder.update { it.copy(centralConnected = false, notifySubscribed = false) }
        compose.setContent { MusicScreen() }
        compose.onNodeWithText("Connect phone in Gadgetbridge").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Pause").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Next track").assertIsNotEnabled()
    }

    @Test
    fun restoredSamsungConnectionDoesNotDependOnReplayedCccdCallback() {
        ProbeStateHolder.update { it.copy(notifySubscribed = false) }
        compose.setContent { MusicScreen() }
        compose.onNodeWithContentDescription("Pause").performScrollTo().performClick()
        assertEquals(listOf("pause"), commands)
    }

    @Test
    fun phonePauseUpdateChangesTheButtonToPlay() {
        compose.setContent { MusicScreen() }
        compose.runOnIdle { MusicStore.onState("pause", 10) }
        compose.onNodeWithContentDescription("Play").performScrollTo().performClick()
        assertEquals(listOf("play"), commands)
    }
}

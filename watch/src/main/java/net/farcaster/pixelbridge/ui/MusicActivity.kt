// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.farcaster.pixelbridge.service.PixelBridgeService

/** Direct target of the watch-face music indicator. */
class MusicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PixelBridgeService.start(this)
        setContent { MusicScreen() }
    }
}

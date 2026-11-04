// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.ui

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import net.farcaster.pixelbridge.R
import net.farcaster.pixelbridge.ble.ProbeStateHolder
import net.farcaster.pixelbridge.data.MusicStore
import net.farcaster.pixelbridge.data.NotificationActions

internal fun sendMusicCommand(context: Context, command: String) {
    if (!NotificationActions.music(command)) {
        Toast.makeText(context, "Command not sent. Reconnect phone in Gadgetbridge.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MusicScreen() {
    val np by MusicStore.state.collectAsStateWithLifecycle()
    val connection by ProbeStateHolder.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val ready = connection.controlsReady && np != null

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
                .rotaryScrollable(RotaryScrollableDefaults.behavior(scrollableState = scroll), focus)
                .verticalScroll(scroll)
                .padding(horizontal = 22.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("MUSIC", color = Color(0xFF8AB4F8), style = MaterialTheme.typography.labelSmall)
            Text(
                np?.track ?: "Nothing playing", style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            np?.artist?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC4C7C5),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (!connection.controlsReady) {
                Text("Connect phone in Gadgetbridge", style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center, color = Color(0xFFFDD663))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MusicButton(R.drawable.ic_music_skip_previous, "Previous track", ready) {
                    sendMusicCommand(context, NotificationActions.MUSIC_PREVIOUS)
                }
                MusicButton(
                    if (np?.playing == true) R.drawable.ic_music_pause else R.drawable.ic_music_play_arrow,
                    if (np?.playing == true) "Pause" else "Play", ready, accent = true,
                ) {
                    sendMusicCommand(context, if (np?.playing == true) NotificationActions.MUSIC_PAUSE else NotificationActions.MUSIC_PLAY)
                }
                MusicButton(R.drawable.ic_music_skip_next, "Next track", ready) {
                    sendMusicCommand(context, NotificationActions.MUSIC_NEXT)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MusicButton(R.drawable.ic_music_volume_down, "Volume down", ready) {
                    sendMusicCommand(context, NotificationActions.MUSIC_VOLUMEDOWN)
                }
                MusicButton(R.drawable.ic_music_volume_up, "Volume up", ready) {
                    sendMusicCommand(context, NotificationActions.MUSIC_VOLUMEUP)
                }
            }
        }
    }
}

@Composable
private fun MusicButton(
    @DrawableRes icon: Int,
    label: String,
    enabled: Boolean,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape)
            .background(if (!enabled) Color(0xFF202124) else if (accent) Color(0xFF315B91) else Color(0xFF3C4043))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(icon), contentDescription = label,
            modifier = Modifier.size(24.dp), alpha = if (enabled) 1f else 0.35f)
    }
}

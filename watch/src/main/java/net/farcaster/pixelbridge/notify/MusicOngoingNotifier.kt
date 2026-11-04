// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.notify

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import net.farcaster.pixelbridge.R
import net.farcaster.pixelbridge.data.NowPlaying
import net.farcaster.pixelbridge.ui.MusicActivity

internal data class MusicActivityState(val active: Boolean, val track: String?, val artist: String?) {
    companion object {
        fun from(np: NowPlaying?, connected: Boolean) = MusicActivityState(
            active = connected && np?.playing == true,
            track = np?.track,
            artist = np?.artist,
        )
    }
}

/** A remote-control Ongoing Activity; audio continues playing on the phone. */
class MusicOngoingNotifier(private val context: Context) {
    private var lastState: MusicActivityState? = null

    init {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Music controls", NotificationManager.IMPORTANCE_LOW),
        )
    }

    @SuppressLint("MissingPermission") // Same runtime grant as bridged notifications.
    fun update(np: NowPlaying?, connected: Boolean) {
        val state = MusicActivityState.from(np, connected)
        if (state == lastState) return
        lastState = state
        if (!state.active) {
            NotificationManagerCompat.from(context).cancel(TAG, ID)
            return
        }
        val openPlayer = PendingIntent.getActivity(
            context, 0,
            Intent(context, MusicActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val track = state.track ?: "Music playing on phone"
        val builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_music_music_note)
            .setContentTitle(track)
            .setContentText(state.artist ?: "Tap to control playback")
            .setContentIntent(openPlayer)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(true)
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        OngoingActivity.Builder(context, TAG, ID, builder)
            .setStaticIcon(R.drawable.ic_music_music_note)
            .setTouchIntent(openPlayer)
            .setStatus(Status.Builder().addTemplate("#track#").addPart("track", Status.TextPart(track)).build())
            .build()
            .apply(context)
        runCatching { NotificationManagerCompat.from(context).notify(TAG, ID, builder.build()) }
            .onFailure { Log.e("MusicOngoing", "Cannot publish music controls", it) }
    }

    fun cancel() {
        NotificationManagerCompat.from(context).cancel(TAG, ID)
        lastState = null
    }

    private companion object {
        const val CHANNEL = "pixelbridge_music"
        const val TAG = "pixelbridge_music"
        const val ID = 2 // The tag prevents collisions with phone notification ids and the BLE service.
    }
}

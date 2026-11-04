// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.notify

import net.farcaster.pixelbridge.data.NowPlaying
import org.junit.Assert.*
import org.junit.Test

class MusicActivityStateTest {
    @Test
    fun indicatorAppearsOnlyDuringConnectedPlayback() {
        val playing = NowPlaying(track = "Track", artist = "Artist", playing = true)
        assertTrue(MusicActivityState.from(playing, true).active)
        assertFalse(MusicActivityState.from(playing, false).active)
        assertFalse(MusicActivityState.from(playing.copy(playing = false), true).active)
        assertFalse(MusicActivityState.from(null, true).active)
    }

    @Test
    fun positionTicksDoNotRepostTheIndicator() {
        val playing = NowPlaying(track = "Track", playing = true)
        assertEquals(MusicActivityState.from(playing, true), MusicActivityState.from(playing.copy(positionMs = 10000), true))
        assertNotEquals(MusicActivityState.from(playing, true), MusicActivityState.from(playing.copy(track = "Next"), true))
    }
}

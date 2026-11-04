// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.ble

/** Serializes ATT notifications. A rejected packet must never leave the queue waiting for a callback. */
internal class NusTxQueue(
    private val sendPacket: (ByteArray) -> Boolean,
    private val onFailure: () -> Unit,
) {
    private val packets = ArrayDeque<ByteArray>()
    private var busy = false

    @Synchronized
    fun enqueue(line: String, chunkSize: Int): Boolean {
        require(chunkSize > 0)
        val bytes = (line + "\r\n").toByteArray(Charsets.UTF_8)
        bytes.asList().chunked(chunkSize).forEach { packets.add(it.toByteArray()) }
        return pump()
    }

    @Synchronized
    fun onPacketSent(success: Boolean) {
        if (!busy) return // A late callback after disconnect/reset belongs to the old transfer.
        busy = false
        if (success) pump() else fail()
    }

    @Synchronized
    fun reset() {
        packets.clear()
        busy = false
    }

    private fun pump(): Boolean {
        if (busy) return true
        val packet = packets.removeFirstOrNull() ?: return true
        busy = true
        if (sendPacket(packet)) return true
        fail()
        return false
    }

    private fun fail() {
        reset()
        // The peer may have received a partial JSON line; reconnect before sending a new one.
        onFailure()
    }
}

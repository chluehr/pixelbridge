// SPDX-License-Identifier: Apache-2.0

package net.farcaster.pixelbridge.ble

import org.junit.Assert.*
import org.junit.Test

class NusTxQueueTest {
    @Test
    fun serializesChunksAndPreservesUtf8AndCrLf() {
        val sent = mutableListOf<ByteArray>()
        val queue = NusTxQueue({ sent.add(it); true }, { fail("Unexpected failure") })
        val line = """{"t":"music","n":"previous","label":"Музыка"}"""
        assertTrue(queue.enqueue(line, 20))
        assertEquals(1, sent.size)
        repeat(10) { queue.onPacketSent(true) }
        assertTrue(sent.all { it.size <= 20 })
        assertArrayEquals((line + "\r\n").toByteArray(), sent.fold(byteArrayOf()) { all, b -> all + b })
    }

    @Test
    fun rejectedPacketDoesNotWaitForeverForMissingCallback() {
        var accepting = false
        var failures = 0
        val sent = mutableListOf<String>()
        val queue = NusTxQueue({ sent.add(it.toString(Charsets.UTF_8)); accepting }, { failures++ })
        assertFalse(queue.enqueue("old", 20))
        assertEquals(1, failures)
        accepting = true
        assertTrue(queue.enqueue("new", 20))
        assertEquals(listOf("old\r\n", "new\r\n"), sent)
    }

    @Test
    fun waitsForCompletionBeforeSendingNextCommand() {
        val sent = mutableListOf<String>()
        val queue = NusTxQueue({ sent.add(it.toString(Charsets.UTF_8)); true }, {})
        queue.enqueue("pause", 20)
        queue.enqueue("next", 20)
        assertEquals(listOf("pause\r\n"), sent)
        queue.onPacketSent(true)
        assertEquals(listOf("pause\r\n", "next\r\n"), sent)
    }

    @Test
    fun failedCompletionDropsPartialLineAndQueuedCommands() {
        val sent = mutableListOf<ByteArray>()
        var failures = 0
        val queue = NusTxQueue({ sent.add(it); true }, { failures++ })
        queue.enqueue("a long command", 5)
        queue.enqueue("next", 20)
        queue.onPacketSent(false)
        queue.onPacketSent(true)
        assertEquals(1, failures)
        assertEquals(1, sent.size)
        queue.enqueue("fresh connection", 20)
        assertEquals("fresh connection\r\n", sent.last().toString(Charsets.UTF_8))
    }

    @Test
    fun disconnectResetDiscardsOldPacketsAndLateCallback() {
        val sent = mutableListOf<ByteArray>()
        val queue = NusTxQueue({ sent.add(it); true }, { fail("Unexpected failure") })
        queue.enqueue("old command", 5)
        queue.reset()
        queue.onPacketSent(true)
        assertEquals(1, sent.size)
        queue.enqueue("new", 20)
        assertEquals("new\r\n", sent.last().toString(Charsets.UTF_8))
    }
}

package de.takeweiland.mods.commons.net

import kotlinx.coroutines.experimental.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Take Weiland
 */
private val pendingResponses = ConcurrentHashMap<Int, CompletableDeferred<out ResponsePacket>>()

private val nextResponseId = AtomicInteger()

internal const val RESPONSE_ID_MASK = 0b0111_1111
internal const val RESPONSE_MARKER_BIT = 0b1000_0000

internal fun reservePacketResponse(deferred: CompletableDeferred<out ResponsePacket>): Int {
    val id = nextResponseId.getAndIncrement() and RESPONSE_ID_MASK
    pendingResponses[id] = deferred
    return id
}

internal fun getPacketResponseFutureAndRemove(id: Int): CompletableDeferred<in ResponsePacket> {
    @Suppress("UNCHECKED_CAST")
    return pendingResponses.remove(id) as CompletableDeferred<in ResponsePacket>
}
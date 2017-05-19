package de.take_weiland.mods.commons.net.simple

import net.minecraft.network.NetworkManager

/**
 * Common supertype for [SimplePacket] and [MultiResultBuilder].
 *
 * @param Result the type produced by sending this to one receiver
 * @param MultiResult the type produced by sending this to multiple receivers
 * @param Builder the type of builder used when sending this to multiple receivers
 *
 * @author diesieben07
 */
@SendableDsl
interface Sendable<out Result, out MultiResult, out Builder : MultiResultBuilder<MultiResult, Builder>> {

    /**
     * Send this to the given `NetworkManager`.
     */
    fun sendTo(manager: NetworkManager): Result

    /**
     * Create a builder for sending this to multiple receivers. Usually used via [sendMulti].
     */
    fun newMultiResultBuilder(): Builder

}

/**
 * Helper method to conveniently send this packet to multiple receivers. Mostly useful for packets with responses.
 *
 * Usage:
 * ```kotlin
 * val responses = packet.sendMulti {
 *     sendTo(player)
 *     sendTo(otherPlayer)
 *     // etc.
 * }
 * ```
 */
inline fun <Result, MultiResult, Builder : MultiResultBuilder<MultiResult, Builder>> Sendable<Result, MultiResult, Builder>.sendMulti(body: Builder.() -> Unit): MultiResult {
    return newMultiResultBuilder().also(body).finish()
}

@DslMarker
internal annotation class SendableDsl
package de.takeweiland.mods.commons.net

import kotlinx.coroutines.experimental.Deferred
import net.minecraft.network.NetworkManager

/**
 * Something that can be sent over the network, base type for packets with and without a result.
 * See [Packet] for usage examples.
 *
 * @author Take Weiland
 */
interface NetworkSendable<out RESULT, out M_RESULT, out BUILDER : NetworkMultiResultBuilder<RESULT, M_RESULT, BUILDER>> {

    /**
     * Send this packet through the given `NetworkManager`.
     */
    fun sendTo(network: NetworkManager): RESULT

    /**
     * Obtain a builder for sending this packet to multiple targets.
     */
    fun newMultiResultBuilder(): BUILDER

}

/**
 * A builder for sending a [NetworkSendable] to multiple targets. Must be "closed" by calling [finish].
 */
interface NetworkMultiResultBuilder<out RESULT, out M_RESULT, out BUILDER : NetworkMultiResultBuilder<RESULT, M_RESULT, BUILDER>> :
    NetworkSendable<RESULT, M_RESULT, BUILDER> {

    /**
     * Finish the multi-target sending operation.
     */
    fun finish(): M_RESULT
}

/**
 * Base interface for implementing a packet without result (`Unit`).
 */
interface SimplePacket : NetworkSendable<Unit, Unit, SimplePacket>, NetworkMultiResultBuilder<Unit, Unit, SimplePacket> {

    override fun newMultiResultBuilder(): SimplePacket = this

    override fun finish() = Unit

    /**
     * Base interface for implementing a packet with results of type `Deferred<R>`. A multi-result is represented as `Map<NetworkManager, Deferred<R>>`.
     */
    interface WithResponse<out R> : NetworkSendable<Deferred<R>, ResponseMultiResult<R>, WithResponseMultiBuilder<R>> {

        override fun newMultiResultBuilder(): WithResponseMultiBuilder<R> {
            return WithResponseMultiBuilderImpl(this)
        }

    }
}

/**
 * Base interface for implementing a multi-result builder for a packet with results of type `Deferred<R>`
 * ([SimplePacket.WithResponse] can be used to implement such a packet).
 */
interface WithResponseMultiBuilder<out R> : NetworkMultiResultBuilder<Deferred<R>, ResponseMultiResult<R>, WithResponseMultiBuilder<R>>

private typealias ResponseMultiResult<R> = Map<NetworkManager, Deferred<R>>

internal class WithResponseMultiBuilderImpl<R>(private val base: NetworkSendable<Deferred<R>, ResponseMultiResult<R>, *>) :
    WithResponseMultiBuilder<R>,
    HashMap<NetworkManager, Deferred<R>>() {

    override fun sendTo(network: NetworkManager): Deferred<R> {
        val def = base.sendTo(network)
        put(network, def)
        return def
    }

    override fun newMultiResultBuilder(): WithResponseMultiBuilder<R> {
        return this
    }

    override fun finish(): ResponseMultiResult<R> {
        return this
    }
}

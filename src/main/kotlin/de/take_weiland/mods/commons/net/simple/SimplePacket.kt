package de.take_weiland.mods.commons.net.simple

import net.minecraft.network.NetworkManager
import java.util.concurrent.CompletionStage

/**
 * A packet with no response.
 *
 * @author diesieben07
 */
interface SimplePacket : Sendable<Unit, Unit, SimplePacket>, MultiResultBuilder<Unit, SimplePacket> {

    override fun newMultiResultBuilder(): SimplePacket {
        return this
    }

    override fun finish() {

    }

    /**
     * A packet with response of type `Result`.
     */
    interface WithResponse<out Result> : Sendable<CompletionStage<out Result>, Map<NetworkManager, CompletionStage<out Result>>, WithResponseMultiBuilder<Result>> {

        override fun newMultiResultBuilder(): WithResponseMultiBuilder<Result> {
            return WithResponseMultiBuilder(this)
        }

        /**
         * Create a packet that ignores the response.
         */
        fun discardResponse(): SimplePacket {
            return object : SimplePacket {

                override fun sendTo(manager: NetworkManager) {
                    this@WithResponse.sendTo(manager)
                }

            }
        }

    }

}
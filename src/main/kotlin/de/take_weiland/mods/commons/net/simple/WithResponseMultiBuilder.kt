package de.take_weiland.mods.commons.net.simple

import net.minecraft.network.NetworkManager
import java.util.concurrent.CompletionStage

/**
 * Builder for the multi-response of a [SimplePacket.WithResponse].
 *
 * @param packet the packet being sent
 *
 * @author diesieben07
 */
class WithResponseMultiBuilder<out Result>(private val packet: SimplePacket.WithResponse<Result>) : MultiResultBuilder<CompletionStage<out Result>, Map<NetworkManager, CompletionStage<out Result>>, WithResponseMultiBuilder<Result>> {

    private val map = HashMap<NetworkManager, CompletionStage<out Result>>()

    override fun sendTo(manager: NetworkManager): CompletionStage<out Result> {
        return packet.sendTo(manager).also { result ->
            map.put(manager, result)
        }
    }

    override fun finish(): Map<NetworkManager, CompletionStage<out Result>> {
        return map
    }

    override fun newMultiResultBuilder(): WithResponseMultiBuilder<Result> {
        return this
    }
}
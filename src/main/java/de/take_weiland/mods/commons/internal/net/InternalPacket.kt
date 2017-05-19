package de.take_weiland.mods.commons.internal.net

import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.simple.SimplePacket
import net.minecraft.network.NetworkManager

/**
 *
 * packet that can be sent through NetworkImpl.
 * @author diesieben07
 */
interface InternalPacket : SimplePacket {

    fun `_sc$internal$channel`(): String

    fun `_sc$internal$expectedSize`(): Int

    @Throws(Exception::class)
    fun `_sc$internal$writeTo`(out: MCDataOutput)

    /**
     *
     * Always called on the network thread.
     * @param side side
     * *
     * @param manager NetworkManager
     */
    fun `_sc$internal$receiveDirect`(side: Byte, manager: NetworkManager)

    override fun sendTo(manager: NetworkManager) {
        NetworkImpl.sendPacket(this, manager)
    }
}

package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.SimplePacketData;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * <p>Does the "magic" behind the Packet class. Implemented using ASM.</p>
 *
 * @author diesieben07
 */
interface PacketAdditionalMethods extends BaseNettyPacket, BaseModPacket {

    @Override
    default void _sc$handle(EntityPlayer player) {
        //noinspection unchecked,rawtypes
        ((BiConsumer) ((SimplePacketData.Normal<?>) _sc$getData()).handler).accept(this, player);
    }

    @Override
    default byte[] _sc$encode() {
        return NetworkImpl.encodePacket((Packet) this, _sc$getData());
    }

    @Override
    default String _sc$channel() {
        return _sc$getData().channel;
    }

    @Override
    default byte _sc$characteristics() {
        return _sc$getData().info;
    }

}

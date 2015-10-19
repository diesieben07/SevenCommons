package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Does the "magic" behind the {@link Packet} class. Implemented using ASM.</p>
 *
 * @author diesieben07
 */
public interface PacketAdditionalMethods extends BaseNettyPacket, BaseModPacket {

    String CLASS_NAME = "de/take_weiland/mods/commons/net/PacketAdditionalMethods";

    @Override
    default void _sc$handle(EntityPlayer player) {
        // ALL THE CASTS!
        //noinspection unchecked,rawtypes
        ((PacketHandler) ((SimplePacketData.Normal<?>) _sc$getData()).handler).handle((Packet) this, player);
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

package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * <p>Does the "magic" behind the Packet class. Implemented using ASM.</p>
 *
 * @author diesieben07
 */
public interface SimplePacketWithoutResponseMagic extends BaseNettyPacket, BaseModPacket {

    String CLASS_NAME = "de/take_weiland/mods/commons/internal/net/SimplePacketWithoutResponseMagic";

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
    default boolean _sc$async() {
        return _sc$getData().async;
    }

}

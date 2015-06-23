package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public interface BaseModPacket extends BaseNettyPacket {

    @Override
    default void _sc$handle(EntityPlayer player) {
        PacketToChannelMap.getData(this).handler.accept(this, player);
    }

    @Override
    default Packet _sc$encodeToPlayer(EntityPlayerMP player) {
        return makePacket(this, S3FPacketCustomPayload::new);
    }

    @Override
    default Packet _sc$encode() {
        return makePacket(this, SevenCommons.proxy.getC17PacketCstr());
    }

    void _sc$writeTo(MCDataOutput out);

    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

    static <P extends BaseModPacket> Packet makePacket(P self, BiFunction<String, byte[], ? extends Packet> constructor) {
        SimplePacketData<P> data = PacketToChannelMap.getData(self);
        MCDataOutput out = Network.newOutput(self.expectedSize() + 1);
        out.writeByte(data.packetID);
        self.writeTo(out);
        return constructor.apply(data.channel, out.toByteArray());
    }

}

package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.net.BasePacket;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import de.take_weiland.mods.commons.internal.net.SimplePacketData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.Predicate;

import static de.take_weiland.mods.commons.internal.net.PacketToChannelMap.getData;

/**
 * @author diesieben07
 */
public interface Packet extends BasePacket, SimplePacket {

    @Override
    void writeTo(MCDataOutput out);

    @Override
    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

    static Side receivingSide(Class<? extends Packet> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(Receiver.class))
                .map(Receiver::value)
                .orElseThrow(() -> new IllegalStateException("Packet missing @Receiver annotation"));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Receiver {

        Side value();

    }

    @Override
    default void sendToServer() {
        SimplePacketData data = PacketToChannelMap.getData(this);
        data.sendToServer(this);
        Network.sendToServer(new RawPacket.UsingCustomPayload() {
            @Override
            public void handle(EntityPlayer player) {
                data.handler.accept(Packet.this, player);
            }

            @Override
            public String channel() {
                return data.channel;
            }

            @Override
            public byte[] write() {
                MCDataOutput out = Network.newOutput(expectedSize());
                out.writeByte(data.packetID);
                writeTo(out);
                return out.toByteArray();
            }
        });
    }

    @Override
    default void sendTo(EntityPlayer player) {
        getData(this).sendTo(this, player);
    }

    @Override
    default void sendTo(Iterable<? extends EntityPlayer> players) {
        getData(this).sendTo(this, players);
    }

    @Override
    default void sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        getData(this).sendTo(this, players, filter);
    }

    interface WithResponse<R extends Packet> extends BasePacket, SimplePacket.WithResponse<R> {

        @Override
        void writeTo(MCDataOutput out);

    }
}

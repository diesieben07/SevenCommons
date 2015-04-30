package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.NetworkChannel;
import de.take_weiland.mods.commons.util.Players;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public final class NetworkChannelImpl<P> implements NetworkChannel<P> {

    final String channel;
    final Function<? super P, ? extends ByteBuf> encoder;
    final Function<? super ByteBuf, ? extends P> decoder;
    final BiConsumer<? super P, ? super EntityPlayer> handler;

    NetworkChannelImpl(String channel, Function<? super P, ? extends ByteBuf> encoder, Function<? super ByteBuf, ? extends P> decoder, BiConsumer<? super P, ? super EntityPlayer> handler) {
        this.channel = channel;
        this.encoder = encoder;
        this.decoder = decoder;
        this.handler = handler;
    }

    void decodeAndHandle(ByteBuf buf, EntityPlayer player) {
        handler.accept(decoder.apply(buf), player);
    }

    Runnable directClientHandler(P packet) {
        BiConsumer<? super P, ? super EntityPlayer> handler = this.handler;
        return () -> handler.accept(packet, Players.getClient());
    }

    Runnable directServerHandler(P packet) {
        BiConsumer<? super P, ? super EntityPlayer> handler = this.handler;
        return () -> handler.accept(packet, Players.getSPOwner());
    }

    @Override
    public void sendToServer(P packet) {
        NetworkImpl.sendToServer(this, packet);
    }

    @Override
    public void sendTo(P packet, EntityPlayer player) {
        NetworkImpl.sendToPlayer(this, packet, player);
    }

    @Override
    public void sendTo(P packet, Iterable<? extends EntityPlayer> players) {
        NetworkImpl.sendToPlayers(this, packet, players);
    }

    @Override
    public void sendTo(P packet, Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        NetworkImpl.sendToPlayers(this, packet, players, filter);
    }

}

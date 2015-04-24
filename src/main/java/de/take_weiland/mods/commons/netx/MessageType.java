package de.take_weiland.mods.commons.netx;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * @author diesieben07
 */
final class MessageType<T> {

    final Function<? super ByteBuf, ? extends T> decoder;
    final BiConsumer<? super T, ? super ByteBuf> encoder;
    final BiConsumer<? super T, ? super EntityPlayer> handler;
    final ToIntFunction<? super T> sizeEstimate;

    MessageType(BiConsumer<? super T, ? super ByteBuf> encoder, Function<? super ByteBuf, ? extends T> decoder, BiConsumer<? super T, ? super EntityPlayer> handler, ToIntFunction<? super T> sizeEstimate) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.handler = handler;
        this.sizeEstimate = sizeEstimate;
    }

    void decodeAndHandle(ByteBuf buf, EntityPlayer player) {
        handler.accept(decoder.apply(buf), player);
    }
}

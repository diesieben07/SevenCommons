package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * @author diesieben07
 */
final class HandlerIDPair {

    final BiConsumer<? super Packet, ? super EntityPlayer> handler;
    final byte id;

    HandlerIDPair(BiConsumer<? super Packet, ? super EntityPlayer> handler, byte id) {
        this.handler = handler;
        this.id = id;
    }
}

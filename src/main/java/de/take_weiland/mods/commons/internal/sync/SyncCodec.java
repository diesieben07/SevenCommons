package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.PacketCodec;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public final class SyncCodec implements PacketCodec<SyncCompanion> {
    @Override
    public byte[] encode(SyncCompanion packet) {
        return new byte[0];
    }

    @Override
    public SyncCompanion decode(byte[] payload) {
        return null;
    }

    @Override
    public void handle(SyncCompanion packet, EntityPlayer player) {

    }

    @Override
    public boolean doCustomLocalHandling(SyncCompanion packet, EntityPlayer player) {
        return false;
    }

    @Override
    public String channel() {
        return "SC|Sync";
    }
}

package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.PacketCodec;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public final class SyncCodec implements PacketCodec<SyncEvent> {

    @Override
    public byte[] encode(SyncEvent packet) {
        MCDataOutput out = Network.newOutput();
        packet.writeTo(out);
        return out.toByteArray();
    }

    @Override
    public void decodeAndHandle(byte[] payload, EntityPlayer player) {
        // caused when packet is received over actual wire
        MCDataInput stream = Network.newInput(payload);
        Scheduler.client().execute(() -> SyncEvent.readAndApply(stream));
    }

    @Override
    public void handle(SyncEvent packet, EntityPlayer player) {
        // only called for local, direct connections, since we override decodeAndHandle
        Scheduler.client().execute(packet::applyDirect);
    }

    @Override
    public String channel() {
        return "SC|Sync";
    }

    @Override
    public SyncEvent decode(byte[] payload) {
        throw new AssertionError("not possible!");
    }
}

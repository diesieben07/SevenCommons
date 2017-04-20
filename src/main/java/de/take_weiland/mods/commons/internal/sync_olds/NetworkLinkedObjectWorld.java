package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.sync.NetworkLinkedObjectType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public class NetworkLinkedObjectWorld implements NetworkLinkedObjectType<World, Void> {
    @Override
    public void write(World object, MCDataOutput out) {
        // no-op
    }

    @Override
    public World read(MCDataInput in, EntityPlayer player) {
        return player.world;
    }

    @Override
    public Void getData(World object) {
        return null;
    }

    @Override
    public World getObject(Void data, EntityPlayer player) {
        return player.world;
    }

    @Override
    public void sendToTracking(World object, SimplePacket packet) {
        packet.sendToAllIn(object);
    }
}

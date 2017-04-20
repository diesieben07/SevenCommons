package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.sync.NetworkLinkedObjectType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class NetworkLinkedObjectEntity implements NetworkLinkedObjectType<Entity, Integer> {
    @Override
    public void write(Entity object, MCDataOutput out) {
        out.writeInt(object.getEntityId());
    }

    @Override
    public Entity read(MCDataInput in, EntityPlayer player) {
        return player.world.getEntityByID(in.readInt());
    }

    @Override
    public Integer getData(Entity object) {
        return object.getEntityId();
    }

    @Override
    public Entity getObject(Integer id, EntityPlayer player) {
        return player.world.getEntityByID(id);
    }

    @Override
    public void sendToTracking(Entity object, SimplePacket packet) {
        packet.sendToAllTracking(object);
    }
}

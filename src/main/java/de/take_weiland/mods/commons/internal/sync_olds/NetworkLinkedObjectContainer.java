package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.sync.NetworkLinkedObjectType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * @author diesieben07
 */
public class NetworkLinkedObjectContainer implements NetworkLinkedObjectType<Container, Integer> {
    @Override
    public void write(Container object, MCDataOutput out) {
        out.writeByte(object.windowId);
    }

    @Override
    public Container read(MCDataInput in, EntityPlayer player) {
        int windowId = in.readInt();
        return getObject(windowId, player);
    }

    @Override
    public Integer getData(Container object) {
        return object.windowId;
    }

    @Override
    public Container getObject(Integer windowId, EntityPlayer player) {
        return player.openContainer.windowId == windowId ? player.openContainer : null;
    }

    @Override
    public void sendToTracking(Container object, SimplePacket packet) {
        packet.sendToViewing(object);
    }
}

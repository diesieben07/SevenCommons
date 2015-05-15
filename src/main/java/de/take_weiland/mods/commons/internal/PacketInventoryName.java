package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@Packet.Receiver(Side.CLIENT)
public final class PacketInventoryName implements Packet {

    private final int windowId;
    private final int invIdx;
    private final String name;

    public PacketInventoryName(int windowId, int invIdx, String name) {
        this.windowId = windowId;
        this.invIdx = invIdx;
        this.name = name;
    }

    PacketInventoryName(MCDataInput in) {
        windowId = in.readByte();
        invIdx = in.readByte();
        name = in.readString();
    }

    @Override
    public void writeTo(MCDataOutput out) {
        out.writeByte(windowId);
        out.writeByte(invIdx);
        out.writeString(name);
    }

    void handle(EntityPlayer player) {
        if (player.openContainer.windowId == windowId) {
            IInventory inv = JavaUtils.get(Containers.getInventories(player.openContainer).asList(), invIdx);
            if (inv instanceof NameableInventory) {
                ((NameableInventory) inv).setCustomName(name);
            }
        }
    }
}

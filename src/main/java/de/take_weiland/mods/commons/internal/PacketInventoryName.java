package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketInventoryName implements Packet {

    private final int windowId;
    private final int invIdx;
    private final ITextComponent name;

    public PacketInventoryName(int windowId, int invIdx, ITextComponent name) {
        this.windowId = windowId;
        this.invIdx = invIdx;
        this.name = name;
    }

    PacketInventoryName(MCDataInput in) {
        windowId = in.readByte();
        invIdx = in.readByte();
        name = ITextComponent.Serializer.jsonToComponent(in.readString());
    }

    @Override
    public void writeTo(MCDataOutput out) {
        out.writeByte(windowId);
        out.writeByte(invIdx);
        out.writeString(ITextComponent.Serializer.componentToJson(name));
    }

    @PacketHandler.ReceivingSide(Side.CLIENT)
    void handle(EntityPlayer player) {
        if (player.openContainer.windowId == windowId) {
            // TODO
            IInventory inv = JavaUtils.get(Containers.getInventories(player.openContainer).asList(), invIdx);
//            if (inv instanceof NameableInventory) {
//                ((NameableInventory) inv).setCustomName(name);
//            }
        }
    }
}

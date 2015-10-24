package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.ItemInventory;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

/**
 * @author diesieben07
 */
public class PacketItemInvUUID implements Packet {

    private final int windowId;
    private final int invIndex;
    private final UUID uuid;

    public PacketItemInvUUID(int windowId, int invIndex, UUID uuid) {
        this.windowId = windowId;
        this.invIndex = invIndex;
        this.uuid = uuid;
    }

    public PacketItemInvUUID(MCDataInput in) {
        this.windowId = in.readByte();
        this.invIndex = in.readVarInt();
        this.uuid = in.readUUID();
    }

    @Override
    public void writeTo(MCDataOutput out) {
        out.writeByte(windowId);
        out.writeVarInt(invIndex);
        out.writeUUID(uuid);
    }

    public void handle(EntityPlayer player) {
        if (player.openContainer.windowId == windowId) {
            List<IInventory> inventories = Containers.getInventories(player.openContainer).asList();
            if (invIndex < inventories.size()) {
                IInventory inv = inventories.get(invIndex);
                if (inv instanceof ItemInventory) {
                    try {
                        itemInvUUIDSetter.invokeExact((ItemInventory) inv, (UUID) uuid);
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }

    private static final MethodHandle itemInvUUIDSetter;
    public static final MethodHandle itemInvUUIDGetter;

    static {
        try {
            Field field = ItemInventory.class.getDeclaredField("uuid");
            field.setAccessible(true);
            itemInvUUIDSetter = MethodHandles.publicLookup().unreflectSetter(field);
            itemInvUUIDGetter = MethodHandles.publicLookup().unreflectGetter(field);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}

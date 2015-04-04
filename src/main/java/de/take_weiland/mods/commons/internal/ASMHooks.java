package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.sync.SyncCompanions;
import de.take_weiland.mods.commons.internal.sync.IEEPSyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtHandler;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class containing methods called from ASM generated code.
 *
 * @author diesieben07
 */
@SuppressWarnings("unused")
public final class ASMHooks {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/ASMHooks";
	public static final String ON_START_TRACKING = "onStartTracking";
	public static final String ON_PLAYER_CLONE = "onPlayerClone";
	public static final String NEW_SYNC_STREAM = "newSyncStream";
	public static final String SEND_SYNC_STREAM = "sendSyncStream";
	public static final String FIND_CONTAINER_INVS = "findContainerInvs";
	public static final String ON_LISTENER_ADDED = "onListenerAdded";
	public static final String IS_USEABLE_CLIENT = "isUseableClient";
    public static final String INVOKE_SYNC_COMP_CHECK = "invokeSyncCompanionCheck";
    public static final String ON_GUI_INIT = "onGuiInit";

    private ASMHooks() { }

    public static void invokeSyncCompanionCheck(Object obj, SyncCompanion companion) {
        if (companion != null) companion.check(obj, false);
    }

    public static final String TICK_IEEP_COMPANIONS = "tickIEEPCompanions";

    public static void tickIEEPCompanions(List<IEEPSyncCompanion> props) {
        if (props != null) {
            // put actual logic into different method, to make this method smaller and more likely
            // to be inlined into the World class (called from there every tick for every entity!)
            tickIEEPCompanionsNonNull(props);
        }
    }

    private static void tickIEEPCompanionsNonNull(List<IEEPSyncCompanion> props) {
        //noinspection ForLoopReplaceableByForEach
        int i = props.size();
	    do {
		    if (--i < 0) return;

		    IEEPSyncCompanion companion = props.get(i);
		    companion.check(companion._sc$ieep, false);
	    } while (true);
    }

    public static final String ON_NEW_ENTITY_PROPS = "onNewEntityProps";

    public static void onNewEntityProps(Entity entity, IExtendedEntityProperties props, String identifier) throws Throwable {
        IEEPSyncCompanion companion = (IEEPSyncCompanion) SyncCompanions.newCompanion(props.getClass());
        if (companion == null) {
            return;
        }

        List<IEEPSyncCompanion> companions = ((EntityProxy) entity)._sc$getPropsCompanions();
        if (companions == null) {
            companions = new ArrayList<>();
            ((EntityProxy) entity)._sc$setPropsCompanions(companions);
        }

        companion._sc$ieep = props;
        companion._sc$entity = entity;
        companion._sc$ident = identifier;

        // maintain ordering in the list
        int len = companions.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            if (companions.get(i)._sc$ident.compareTo(identifier) >= 0) {
                index = i;
                break;
            }
        }
        companions.add(index, companion);
    }

	public static final String WRITE_NBT_HOOK = "writeToNbtHook";

	public static void writeToNbtHook(Object obj, NBTTagCompound nbt) {
		ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
		if (handler != null) {
			handler.write(obj, nbt);
		}
	}

	public static final String READ_NBT_HOOK = "readFromNbtHook";

	public static void readFromNbtHook(Object obj, NBTTagCompound nbt) {
		ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
		if (handler != null) {
			handler.read(obj, nbt);
		}
	}

	public static final String IEEP_READ_NBT_HOOK = "readFromNBTIEEP";

	public static void readFromNBTIEEP(Object obj, String ident, NBTTagCompound nbt) {
		ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
		if (handler != null) {
			handler.read(obj, nbt.getCompoundTag(ident));
		}
	}

	public static final String IEEP_WRITE_NBT_HOOK = "writeToNBTIEEP";

	public static void writeToNBTIEEP(Object obj, String ident, NBTTagCompound nbt) {
		ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
		if (handler != null) {
			handler.write(obj, NBT.getOrCreateCompound(nbt, ident));
		}
	}

    @SideOnly(Side.CLIENT)
	public static boolean isUseableClient(Slot slot) {
		return slot != null && slot.canTakeStack(Minecraft.getMinecraft().thePlayer);
	}

	public static void onListenerAdded(Container container, ICrafting listener) {
		if (listener instanceof EntityPlayerMP) {
			List<IInventory> invs = Containers.getInventories(container).asList();
			for (int i = 0, len = invs.size(); i < len; i++) {
				IInventory inv = invs.get(i);
				if (inv instanceof NameableInventory && ((NameableInventory) inv).hasCustomName()) {
					new PacketInventoryName(container.windowId, i, ((NameableInventory) inv).getCustomName()).sendTo((EntityPlayerMP) listener);
				}
			}
		}
	}

	public static ImmutableSet<IInventory> findContainerInvs(Container container) {
		IInventory last = null;
		ImmutableSet.Builder<IInventory> builder = ImmutableSet.builder();

		@SuppressWarnings("unchecked")
		List<Slot> slots = container.inventorySlots;
		for (Slot slot : slots) {
			if (slot.inventory != last) {
				builder.add((last = slot.inventory));
			}
		}
		return builder.build();
	}

    public static void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}

	@SideOnly(Side.CLIENT)
	public static void onGuiInit(GuiScreen gui) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, SCReflector.instance.getButtonList(gui)));
	}

	public static void onStartTracking(EntityPlayer player, Entity tracked) {
		MinecraftForge.EVENT_BUS.post(new PlayerStartTrackingEvent(player, tracked));

//		PacketSyncPropsIDs.sendToIfNeeded(player, tracked);
	}

	private static final int SIGNED_SHORT_BITS = 0b0111_1111_1111_1111;
	private static final int SHORT_MS_BIT = 0b1000_0000_0000_0000;

	public static void writeExtPacketLen(DataOutput out, int len) throws IOException {
		int leftover = (len & ~SIGNED_SHORT_BITS) >>> 15;
		if (leftover != 0) {
			out.writeShort(len & SIGNED_SHORT_BITS | SHORT_MS_BIT);
			out.writeByte(leftover);
		} else {
			out.writeShort(len & SIGNED_SHORT_BITS);
		}
	}

	public static int readExtPacketLen(DataInput in) throws IOException {
		int low = in.readUnsignedShort();
		if ((low & SHORT_MS_BIT) != 0) {
			int hi = in.readUnsignedByte();
			return (low & SIGNED_SHORT_BITS) | (hi << 15);
		} else {
			return low;
		}
	}

	public static int additionalPacketSize(Packet250CustomPayload packet) {
		if ((packet.length & 0b0111_1111_1000_0000_0000_0000) != 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public static int additionalPacketSize(int len) {
		if ((len & 0b0111_1111_1000_0000_0000_0000) != 0) {
			return 1;
		} else {
			return 0;
		}
	}

}

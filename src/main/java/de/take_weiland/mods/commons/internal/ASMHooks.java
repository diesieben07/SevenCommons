package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.internal.sync.SyncerCompanion;
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

    public static void invokeSyncCompanionCheck(Object obj, SyncerCompanion companion) {
        if (companion != null) companion.check(obj, false);
    }

    public static final String TICK_SYNC_PROPS = "tickSyncProps";

    public static void tickSyncProps(List<SyncedEntityProperties> props) {
        if (props != null) {
            // put actual logic into different method, to make this method smaller and more likely
            // to be inlined into the World class (called from there every tick for every entity!)
            tickPropsNonNull(props);
        }
    }

    private static void tickPropsNonNull(List<SyncedEntityProperties> props) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, len = props.size(); i < len; i++) {
            props.get(i)._sc$syncprops$tick();
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

	public static MCDataOutputStream newSyncStream(Object object, SyncType type) {
		MCDataOutputStream out = (MCDataOutputStream) SevenCommons.packets.createStream(SevenCommons.SYNC_PACKET_ID);
		out.writeEnum(type);
		type.writeObject(object, out);
		return out;
	}

	public static void sendSyncStream(Object object, SyncType type, MCDataOutputStream out) {
		type.sendPacket(object, SevenCommons.packets.makePacket(out));
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

		PacketSyncPropsIDs.sendToIfNeeded(player, tracked);
	}

	public static final String ON_NEW_ENTITY_PROPS = "onNewEntityProps";

	public static void onNewEntityProps(Entity entity, IExtendedEntityProperties props, String identifier) {
		if (entity.worldObj.isRemote || !(props instanceof SyncedEntityProperties)) {
			return;
		}
		List<SyncedEntityProperties> syncedProps = ((EntityProxy) entity)._sc$getSyncedProps();
		if (syncedProps == null) {
			((EntityProxy) entity)._sc$setSyncedProps((syncedProps = new ArrayList<>()));
		}
		SyncedEntityProperties syncedProp = (SyncedEntityProperties) props;

		if (syncedProp._sc$syncprops$name() != null) {
			throw new IllegalArgumentException("@Sync used in IExtendedEntityProperties requires one instance per entity");
		}

		syncedProp._sc$syncprops$setName(identifier);
		syncedProp._sc$syncprops$setOwner(entity);
		syncedProp._sc$syncprops$setIndex(syncedProps.size());


		syncedProps.add(syncedProp);
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

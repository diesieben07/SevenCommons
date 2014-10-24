package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.MinecraftForge;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Type;

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
	public static final String FIND_ENUM_SET_TYPE = "findEnumSetType";

	private ASMHooks() { }

	public static MCDataOutputStream newSyncStream(Object object, SyncType type) {
		MCDataOutputStream out = SevenCommons.packets.createStream(SevenCommons.SYNC_PACKET_ID);
		out.writeEnum(type);
		type.writeObject(object, out);
		return out;
	}

	public static void sendSyncStream(Object object, SyncType type, MCDataOutputStream out) {
		type.sendPacket(object, SevenCommons.packets.makePacket(out));
	}

	private static final Type iterableComponent = Iterable.class.getTypeParameters()[0];
	public static Class<?> findEnumSetType(Type genericType) {
		return TypeToken.of(genericType).resolveType(iterableComponent).getRawType();
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

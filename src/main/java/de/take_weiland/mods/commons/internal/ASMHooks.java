package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.util.MiscUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.MinecraftForge;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A class containing methods called from ASM generated code.
 * @author diesieben07
 *
 */
public final class ASMHooks {

	private ASMHooks() { }

	private static final int ZOMBIE_IS_CONVERTING_FLAG = 14;
	
	public static void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}
	
	public static EntityVillager onZombieConvert(EntityZombie zombie, EntityVillager villager) {
		ZombieConvertEvent event = new ZombieConvertEvent(zombie, villager);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			zombie.getDataWatcher().updateObject(ZOMBIE_IS_CONVERTING_FLAG, Byte.valueOf((byte)0)); // reset the isConverting flag if the event was canceled
			return null;
		} else {
			return event.villager;
		}
	}

	@SideOnly(Side.CLIENT)
	public static void onGuiInit(GuiScreen gui) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, MiscUtil.getReflector().getButtonList(gui)));
	}
	
	public static void onStartTracking(EntityPlayer player, Entity tracked) {
		SyncASMHooks.syncEntityPropertyIds(player, tracked);
		MinecraftForge.EVENT_BUS.post(new PlayerStartTrackingEvent(player, tracked));
	}

	public static void writeVarShort(DataOutput out, int i) throws IOException {
		int low =   i & 0b0000_0000_0111_1111_1111_1111;
		int high = (i & 0b0111_1111_1000_0000_0000_0000) >> 15;
		if (high != 0) {
			low |= 0b1000_0000_0000_0000;
		}
		out.writeShort(low);
		if (high != 0) {
			out.writeByte(high);
		}
	}

	public static int readVarShort(DataInput in) throws IOException {
		int low = in.readUnsignedShort();
		int high = (low & 0b1000_0000_0000_0000) != 0 ? in.readUnsignedByte() : 0;

		return ((high & 0b1111_1111) << 15) | (low & 0b0111_1111_1111_1111);
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

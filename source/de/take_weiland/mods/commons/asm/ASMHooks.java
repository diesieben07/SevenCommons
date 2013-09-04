package de.take_weiland.mods.commons.asm;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Strings;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.LivingBreedEvent;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.UnsignedShorts;

/**
 * A class containing methods called from ASM generated code.<br>
 * Do not use in mod code.
 * @author diesieben07
 *
 */
public final class ASMHooks {

	private ASMHooks() { }
	
	private static final int ZOMBIE_IS_CONVERTING_FLAG = 14;
	
	public static final void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}
	
	public static final void onLivingBreed(EntityAnimal animal, EntityAnimal mate, EntityAgeable child) {
//		TODO: implement code to actually change the amount of xp being spawned!
		MinecraftForge.EVENT_BUS.post(new LivingBreedEvent(animal, mate, child));
	}
	
	public static final boolean onZombieConvert(EntityZombie zombie) {
		// TODO: handle the villager!
		if (MinecraftForge.EVENT_BUS.post(new ZombieConvertEvent(zombie, null))) {
			zombie.getDataWatcher().updateObject(ZOMBIE_IS_CONVERTING_FLAG, Byte.valueOf((byte)0)); // reset the isConverting flag if the event was canceled
			return true;
		} else {
			return false;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static final void onGuiInit(GuiScreen gui, List<GuiButton> buttons) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, buttons));
	}
	
	public static boolean syncCompare(FluidStack oldValue, FluidStack newValue) {
		return newValue == null ? oldValue != null : oldValue == null ? true : !newValue.isFluidStackIdentical(oldValue);
	}
	
	public static boolean syncCompare(int oldValue, int newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(short oldValue, short newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(byte oldValue, byte newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(long oldValue, long newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(float oldValue, float newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(double oldValue, double newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(boolean oldValue, boolean newValue) {
		return oldValue != newValue;
	}
	
	public static boolean syncCompare(String oldValue, String newValue) {
		return oldValue == null ? newValue != null : !oldValue.equals(newValue);
	}
	
	public static void syncSend(ByteArrayDataOutput out, FluidStack fluid) {
		if (fluid == null) {
			out.writeShort(0);
		} else {
			int fluidId = fluid.fluidID & 0x7FFF; // leave highest bit out for "hasTag", makes 32767 fluid ids possible which should be enough :D
			int hasTagAndFluidId = fluidId | (fluid.tag == null ? 0x0000 : 0x8000);
			out.writeShort(hasTagAndFluidId);
			out.writeShort(UnsignedShorts.checkedCast(fluid.amount));
			
			if (fluid.tag != null) {
				try {
					byte[] tagData = CompressedStreamTools.compress(fluid.tag);
					out.writeShort(UnsignedShorts.checkedCast(tagData.length));
					out.write(tagData);
				} catch (IOException e) {
					SevenCommons.LOGGER.severe("Unexpected IOException during FieldSyncing!");
					e.printStackTrace();
				}
			}
		}

	}
	
	public static void syncSend(ByteArrayDataOutput out, boolean value) {
		out.writeBoolean(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, byte value) {
		out.writeByte(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, short value) {
		out.writeShort(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, int value) {
		out.writeInt(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, long value) {
		out.writeLong(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, float value) {
		out.writeFloat(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, double value) {
		out.writeDouble(value);
	}
	
	public static void syncSend(ByteArrayDataOutput out, String value) {
		out.writeUTF(Strings.nullToEmpty(value));
	}
	
	public static boolean syncReceive(boolean marker, ByteArrayDataInput in) {
		return in.readBoolean();
	}
	
	public static byte syncReceive(byte marker, ByteArrayDataInput in) {
		return in.readByte();
	}
	
	public static short syncReceive(short marker, ByteArrayDataInput in) {
		return in.readShort();
	}
	
	public static int syncReceive(int marker, ByteArrayDataInput in) {
		return in.readInt();
	}
	
	public static long syncReceive(long marker, ByteArrayDataInput in) {
		return in.readLong();
	}
	
	public static float syncReceive(float marker, ByteArrayDataInput in) {
		return in.readFloat();
	}
	
	public static double syncReceive(double marker, ByteArrayDataInput in) {
		return in.readDouble();
	}
	
	public static String syncReceive(String marker, ByteArrayDataInput in) {
		return in.readUTF();
	}
	
	public static FluidStack syncReceive(FluidStack old, ByteArrayDataInput in) {
		int fluidIdAndHasTag = in.readUnsignedShort();
		if (fluidIdAndHasTag == 0) {
			return null;
		} else {
			int amount = in.readUnsignedShort();
			
			int fluidId = fluidIdAndHasTag & 0x7FFF;
			boolean hasTag = (fluidIdAndHasTag & 0x8000) != 0;
			NBTTagCompound nbt = null;
			if (hasTag) {
				byte[] nbtBytes;
				try {
					nbtBytes = Packet.readBytesFromStream(in);
					nbt = CompressedStreamTools.decompress(nbtBytes);
				} catch (IOException e) {
					SevenCommons.LOGGER.severe("Unexpected IOException during FieldSync!");
					e.printStackTrace();
				}
			}
			
			if (old == null) {
				old = new FluidStack(fluidId, amount);
			} else {
				old.fluidID = fluidId;
				old.amount = amount;
			}
			
			old.tag = nbt;
			return old;
		}
	}
	
}

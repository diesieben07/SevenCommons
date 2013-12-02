package de.take_weiland.mods.commons.asm;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.LivingBreedEvent;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;

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
//	
//	public static void syncSend(MinecraftDataOutput out, FluidStack fluid) {
//		out.writeFluidStack(fluid);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, boolean value) {
//		out.writeBoolean(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, byte value) {
//		out.writeByte(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, short value) {
//		out.writeShort(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, int value) {
//		out.writeInt(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, long value) {
//		out.writeLong(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, float value) {
//		out.writeFloat(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, double value) {
//		out.writeDouble(value);
//	}
//	
//	public static void syncSend(MinecraftDataOutput out, String value) {
//		out.writeUTF(Strings.nullToEmpty(value));
//	}
//	
//	public static boolean syncReceive(boolean marker, MinecraftDataInput in) {
//		return in.readBoolean();
//	}
//	
//	public static byte syncReceive(byte marker, MinecraftDataInput in) {
//		return in.readByte();
//	}
//	
//	public static short syncReceive(short marker, MinecraftDataInput in) {
//		return in.readShort();
//	}
//	
//	public static int syncReceive(int marker, MinecraftDataInput in) {
//		return in.readInt();
//	}
//	
//	public static long syncReceive(long marker, MinecraftDataInput in) {
//		return in.readLong();
//	}
//	
//	public static float syncReceive(float marker, MinecraftDataInput in) {
//		return in.readFloat();
//	}
//	
//	public static double syncReceive(double marker, MinecraftDataInput in) {
//		return in.readDouble();
//	}
//	
//	public static String syncReceive(String marker, MinecraftDataInput in) {
//		return in.readUTF();
//	}
//	
//	public static FluidStack syncReceive(FluidStack old, MinecraftDataInput in) {
//		return in.readFluidStack();
//	}
	
}

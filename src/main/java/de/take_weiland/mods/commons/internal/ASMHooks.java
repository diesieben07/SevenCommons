package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.LivingBreedEvent;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

/**
 * A class containing methods called from ASM generated code.
 * @author diesieben07
 *
 */
@SuppressWarnings("unused") // called from ASM generated code only
public final class ASMHooks {

	private ASMHooks() { }
	
	private static final int ZOMBIE_IS_CONVERTING_FLAG = 14;
	
	public static void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}
	
	public static void onLivingBreed(EntityAnimal animal, EntityAnimal mate, EntityAgeable child) {
//		TODO: implement code to actually change the amount of xp being spawned!
		MinecraftForge.EVENT_BUS.post(new LivingBreedEvent(animal, mate, child));
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
	public static void onGuiInit(GuiScreen gui, List<GuiButton> buttons) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, buttons));
	}
	
	public static void onStartTracking(EntityPlayer player, Entity tracked) {
		SyncASMHooks.syncEntityPropertyIds(player, tracked);
		MinecraftForge.EVENT_BUS.post(new PlayerStartTrackingEvent(player, tracked));
	}
	
}

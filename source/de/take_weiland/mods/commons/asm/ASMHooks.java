package de.take_weiland.mods.commons.asm;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;


import de.take_weiland.mods.commons.event.LivingBreedEvent;
import de.take_weiland.mods.commons.event.PlaceBlockEvent;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.util.ModdingUtils;

/**
 * A class containing methods called from ASM generated code.<br>
 * Do not use.
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
	
	public static final boolean onBlockPlacePre(EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, ItemStack item) {
		return MinecraftForge.EVENT_BUS.post(new PlaceBlockEvent.Pre(player, world, x, y, z, side, hitX, hitY, hitZ, item));
	}
	
	public static final void onBlockPlacePost(EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, ItemStack item) {
		MinecraftForge.EVENT_BUS.post(new PlaceBlockEvent.Post(player, world, x, y, z, side, hitX, hitY, hitZ, item));
	}
	
	public static final void onGuiInit(GuiScreen gui, List<GuiButton> buttons) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, buttons));
	}
	
	public static final void setActivePlayer(EntityPlayer player) {
		ModdingUtils.setActivePlayer(player);
	}
	
	public static final void resetActivePlayer() {
		ModdingUtils.setActivePlayer(null);
	}
}

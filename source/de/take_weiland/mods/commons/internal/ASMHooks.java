package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import de.take_weiland.mods.commons.event.LivingBreedEvent;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;

public final class ASMHooks {

	private ASMHooks() { }
	
	private static final int ZOMBIE_IS_CONVERTING_FLAG = 14;
	
	public static final void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}
	
	public static final void onLivingBreed(EntityAnimal animal, EntityAnimal mate, EntityAgeable child) {
		MinecraftForge.EVENT_BUS.post(new LivingBreedEvent(animal, mate, child));
	}
	
	public static final boolean onZombieConvert(EntityZombie zombie) {
		if (MinecraftForge.EVENT_BUS.post(new ZombieConvertEvent(zombie))) {
			zombie.getDataWatcher().updateObject(ZOMBIE_IS_CONVERTING_FLAG, Byte.valueOf((byte)0)); // reset the isConverting flag if the event was canceled
			return true;
		} else {
			return false;
		}
	}
	
}

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
	
	public static final void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}
	
	public static final void onLivingBreed(EntityAnimal animal, EntityAnimal mate, EntityAgeable child) {
		MinecraftForge.EVENT_BUS.post(new LivingBreedEvent(animal, mate, child));
	}
	
	public static final boolean onZombieConvert(EntityZombie zombie) {
		return MinecraftForge.EVENT_BUS.post(new ZombieConvertEvent(zombie));
	}
	
}

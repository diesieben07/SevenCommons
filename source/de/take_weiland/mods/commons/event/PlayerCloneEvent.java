package de.take_weiland.mods.commons.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerCloneEvent extends PlayerEvent {

	public final EntityPlayer newPlayer;
	
	public PlayerCloneEvent(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		super(oldPlayer);
		this.newPlayer = newPlayer;
	}

}

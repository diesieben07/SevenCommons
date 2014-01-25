package de.take_weiland.mods.commons.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerStartTrackingEvent extends PlayerEvent {

	public final Entity tracked;
	
	public PlayerStartTrackingEvent(EntityPlayer player, Entity tracked) {
		super(player);
		this.tracked = tracked;
	}

}

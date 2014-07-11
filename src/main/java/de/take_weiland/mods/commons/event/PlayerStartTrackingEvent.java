package de.take_weiland.mods.commons.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Fired when a player starts tracking an Entity
 */
public class PlayerStartTrackingEvent extends PlayerEvent {

	/**
	 * the entity now being tracked
	 */
	public final Entity tracked;

	public PlayerStartTrackingEvent(EntityPlayer player, Entity tracked) {
		super(player);
		this.tracked = tracked;
	}

}

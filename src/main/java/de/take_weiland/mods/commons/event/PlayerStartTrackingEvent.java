package de.take_weiland.mods.commons.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * <p>Fired when a player starts tracking an Entity.</p>
 * <p>To be replaced by the Forge version in 1.7.</p>
 */
public class PlayerStartTrackingEvent extends PlayerEvent {

	/**
	 * <p>The Entity now being tracked.</p>
	 */
	public final Entity tracked;

	public PlayerStartTrackingEvent(EntityPlayer player, Entity tracked) {
		super(player);
		this.tracked = tracked;
	}

}

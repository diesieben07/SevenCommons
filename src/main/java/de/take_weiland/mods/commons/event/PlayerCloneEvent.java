package de.take_weiland.mods.commons.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Fired when a player gets a new instance (happens on respawn)<br>
 * Mostly used to prevent your {@link net.minecraftforge.common.IExtendedEntityProperties IExtendedEntityProperties} from vanishing on respawn
 *
 * @author diesieben07
 */
public final class PlayerCloneEvent extends PlayerEvent {

	/**
	 * the new player instance
	 */
	public final EntityPlayer newPlayer;

	public PlayerCloneEvent(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		super(oldPlayer);
		this.newPlayer = newPlayer;
	}

}

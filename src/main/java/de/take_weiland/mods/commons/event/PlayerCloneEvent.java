package de.take_weiland.mods.commons.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * <p>Fired when a player gets a new instance (happens on respawn).</p>
 * <p>Mostly used to prevent your {@link net.minecraftforge.common.IExtendedEntityProperties IExtendedEntityProperties} from vanishing on respawn.</p>
 * <p>To be replaced by the Forge version in 1.7.</p>
 *
 * @author diesieben07
 */
public final class PlayerCloneEvent extends PlayerEvent {

	/**
	 * <p>The new player.</p>
	 */
	public final EntityPlayer oldPlayer;

	public PlayerCloneEvent(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		super(newPlayer);
		this.oldPlayer = oldPlayer;
	}

}

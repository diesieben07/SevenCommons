package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeDirection;

import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Utilities regarding Entities.</p>
 *
 * @author diesieben07
 * @see net.minecraft.entity.Entity
 */
public final class Entities {

	/**
	 * <p>Get an NBTTagCompound for storing custom data about the given Entity.</p>
	 * <p>If the entity is a player, this data is not persisted through death.</p>
	 *
	 * @param entity the Entity
	 * @param key    a unique key for your data, your ModId is a good choice
	 * @return an NBTTagCompound
	 */
	public static NBTTagCompound getData(Entity entity, String key) {
		return NBT.getOrCreateCompound(getData(entity, false), key);
	}

	/**
	 * <p>Get an NBTTagCompound for storing data about the given entity.</p>
	 * <p>If the entity is a player, this data is persisted through death.</p>
	 *
	 * @param entity the Entity
	 * @param key    a unique key for your data, your ModId is a good choice
	 * @return an NBTTagCompound
	 */
	public static NBTTagCompound getPersistedData(Entity entity, String key) {
		return NBT.getOrCreateCompound(getData(entity, entity instanceof EntityPlayer), key);
	}

	/**
	 * <p>Get an NBTTagCompound for storing data about the given player.</p>
	 * <p>This data is persisted through death.</p>
	 *
	 * @param player the player
	 * @param key    a unique key for your data, your ModId is a good choice
	 * @return an NBTTagCompound
	 */
	public static NBTTagCompound getPersistedData(EntityPlayer player, String key) {
		return NBT.getOrCreateCompound(getData(player, true), key);
	}

	private static NBTTagCompound getData(Entity entity, boolean usePersisted) {
		if (usePersisted) {
			return NBT.getOrCreateCompound(entity.getEntityData(), EntityPlayer.PERSISTED_NBT_TAG);
		} else {
			return entity.getEntityData();
		}
	}

	/**
	 * <p>Get all players tracking the given Entity.</p>
	 * <p>To send a packet to all tracking players use {@link de.take_weiland.mods.commons.net.Packets#sendToAllTracking(net.minecraft.network.packet.Packet, net.minecraft.entity.Entity)} instead.</p>
	 * <p>This method must only be called on the logical server and the returned Set must not be modified.</p>
	 *
	 * @param entity the Entity
	 * @return a Set of players tracking the entity
	 */
	@SuppressWarnings("unchecked")
	public static Set<EntityPlayerMP> getTrackingPlayers(Entity entity) {
		checkArgument(Sides.logical(entity).isServer(), "Cannot get tracking players on the client");
		EntityTrackerEntry entry = (EntityTrackerEntry) SCReflector.instance.getTrackerMap(((WorldServer) entity.worldObj).getEntityTracker()).lookup(entity.entityId);
		return entry == null ? Collections.emptySet() : entry.trackingPlayers;
	}

	/**
	 * <p>Get the cardinal direction the given Entity is facing towards.</p>
	 *
	 * @param entity the Entity
	 * @return a cardinal direction (one of {@link net.minecraftforge.common.ForgeDirection#NORTH},
	 * {@link net.minecraftforge.common.ForgeDirection#WEST}, {@link net.minecraftforge.common.ForgeDirection#SOUTH},
	 * {@link net.minecraftforge.common.ForgeDirection#EAST})
	 */
	public static ForgeDirection getFacing(Entity entity) {
		int dir = MathHelper.floor_double((entity.rotationYaw * 4 / 360) + 0.5) & 3;
		return ForgeDirection.VALID_DIRECTIONS[Direction.directionToFacing[dir]];
	}

	private Entities() {
	}

}

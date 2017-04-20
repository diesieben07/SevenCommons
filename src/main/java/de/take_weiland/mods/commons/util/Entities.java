package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Set;

import static java.lang.invoke.MethodHandles.publicLookup;

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
     * <p>This method must only be called on the logical server and the returned Set must not be modified.</p>
     *
     * @param entity the Entity
     * @return a Set of players tracking the entity
     */
    public static Set<EntityPlayerMP> getTrackingPlayers(Entity entity) {
        if (entity.world.isRemote) {
            throw new IllegalArgumentException("Cannot get tracking players on the client");
        }
        //noinspection unchecked
        return (Set<EntityPlayerMP>) ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(entity);
    }

    private static final MethodHandle trackerMapGet;

    static {
        try {
            Field field = EntityTracker.class.getDeclaredField(MCPNames.field(SRGConstants.F_TRACKED_ENTITY_IDS));
            field.setAccessible(true);
            trackerMapGet = publicLookup().unreflectGetter(field);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private Entities() {
    }

}

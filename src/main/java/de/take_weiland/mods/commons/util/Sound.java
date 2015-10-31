package de.take_weiland.mods.commons.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * <p>Base interface for implementing a Sound. Usually implemented on an {@code Enum} to implement a list of sounds in
 * your mod.</p>
 *
 * @author diesieben07
 */
public interface Sound {

    /**
     * <p>The resource domain for this sound.</p>
     *
     * @return the resource domain
     */
    String getDomain();

    /**
     * <p>The name for this sound.</p>
     *
     * @return the name
     */
    String getName();

    /**
     * <p>The full name for this sound, so {@code getDomain() + ':' + getName()}.</p>
     *
     * @return the full name for this sound
     */
    default String fullName() {
        return getDomain() + ':' + getName();
    }

    /**
     * <p>Play this sound at the given entity with volume 1 and pitch 1.</p>
     * <p>This method behaves exactly like {@link Entity#playSound(String, float, float)}: for normal entities it only plays
     * the sound on the server; for players the client plays the sound and the server sends the sound to everyone but
     * the player.</p>
     *
     * @param e the entity
     */
    default void playAt(Entity e) {
        playAt(e, 1, 1);
    }

    /**
     * <p>Play this sound at the given entity with the given volume and pitch.</p>
     * <p>This method behaves exactly like {@link Entity#playSound(String, float, float)}: for normal entities it only plays
     * the sound on the server; for players the client plays the sound and the server sends the sound to everyone but
     * the player.</p>
     *
     * @param e      the entity
     * @param volume the volume
     * @param pitch  the pitch
     */
    default void playAt(Entity e, float volume, float pitch) {
        e.playSound(fullName(), volume, pitch);
    }

    /**
     * <p>Play this sound at the given entity with the given volume and pitch.</p>
     * <p>As opposed to {@link #playAt(Entity, float, float)}, this method only operates when called on the server and
     * uses packets for all players to play the sound.</p>
     *
     * @param entity the entity
     * @param volume the volume
     * @param pitch  the pitch
     */
    default void playAtServer(Entity entity, float volume, float pitch) {
        entity.worldObj.playSoundAtEntity(entity, fullName(), volume, pitch);
    }

    /**
     * <p>Play this sound at the given location with volume 1 and pitch 1.</p>
     *
     * @param world the world
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     */
    default void play(World world, double x, double y, double z) {
        play(world, x, y, z, 1, 1);
    }

    /**
     * <p>Play this sound at the given location with the given volume and pitch.</p>
     *
     * @param world  the world
     * @param volume the volume
     * @param pitch  the pitch
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param z      the z coordinate
     */
    default void play(World world, double x, double y, double z, float volume, float pitch) {
        if (world.isRemote) {
            world.playSound(x, y, z, fullName(), volume, pitch, false);
        } else {
            world.playSoundEffect(x, y, z, getName(), volume, pitch); // does nothing on client
        }
    }


}

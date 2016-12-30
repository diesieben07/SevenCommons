package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>A description for a type of object that occurs on the server and has a mirrored instance on the client, such as a TileEntity.</p>
 * <p>Every object of this type must have an object identifying it, required to be immutable, in the case of a TileEntity
 * this would be the TileEntity's position represented by a {@code BlockPos}.</p>
 *
 * @author diesieben07
 */
public interface NetworkLinkedObjectType<T, DATA> {

    /**
     * <p>Write a representation of the identifier for the given object to the given stream.</p>
     *
     * @param object the object to encode
     * @param out    the stream
     */
    void write(T object, MCDataOutput out);

    /**
     * <p>Find the mirrored client instance of the object encoded in the given stream, as encoded by {@link #write(Object, MCDataOutput)}.</p>
     *
     * @param in     the stream
     * @param player the client player, usually used for accessing the world
     * @return the mirrored client instance
     */
    T read(MCDataInput in, EntityPlayer player);

    /**
     * <p>Get the identifying object for the given object. The returned object must either by immutable or it must not
     * be modified after being returned from this object.</p>
     *
     * @param object the object to identify
     * @return the identifying object
     */
    DATA getData(T object);

    /**
     * <p>Find the mirrored client instance of the object represented by the given identifying object.</p>
     *
     * @param data   the identifying object
     * @param player the client player, usually used for accessing the world
     * @return the mirrored client instance
     */
    T getObject(DATA data, EntityPlayer player);

    /**
     * <p>Send the given packet to all players tracking the given object.</p>
     *
     * @param object the object
     * @param packet the packet to send
     */
    void sendToTracking(T object, SimplePacket packet);

}

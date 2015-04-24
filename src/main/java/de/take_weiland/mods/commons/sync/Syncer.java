package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import io.netty.buffer.ByteBuf;

/**
 * <p>Support for syncing of a Type {@code V}.</p>
 * <p>An optional companion type may be specified to store additional data to enable syncing.</p>
 *
 * @author diesieben07
 */
public interface Syncer<V, C> {

    /**
     * <p>The companion type for this Syncer.</p>
     * <p>The initial value for the companion will be {@code null} for reference types and {@code 0} for primitives.</p>
     */
    Class<C> getCompanionType();

    /**
     * <p>Check if the value has changed since the last time {@link #writeAndUpdate(Object, Object, MCDataOutput)} was called.</p>
     * @param value the value
     * @param companion the companion
     * @return false if the value has changed
     */
    boolean equal(V value, C companion);

    /**
     * <p>Called if {@link #equal(Object, Object)} returns {@code false}. This method needs to writeTo any necessary data to the
     * MCDataOutput and update any data in the companion to ensure that {@code equal} returns false again until the next update.</p>
     * @param value the value
     * @param companion the companion
     * @param out the MCDataOutput
     * @return a potentially new value for the companion
     */
    C writeAndUpdate(V value, C companion, ByteBuf out);

    /**
     * <p>Called to read a new value from the MCDataInput.</p>
     * @param value the value
     * @param companion the companion
     * @param in the MCDataInput
     * @return a potentially new value
     */
    V read(V value, C companion, ByteBuf in);

}

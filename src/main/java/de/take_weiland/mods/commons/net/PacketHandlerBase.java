package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import net.minecraftforge.fml.relauncher.Side;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author diesieben07
 */
public interface PacketHandlerBase extends Serializable {

    default Optional<Side> receivingSide() {
        return NetworkImpl.findReceivingSideReflectively(this);
    }

    default boolean isAsync() {
        return NetworkImpl.findIsAsyncReflectively(this);
    }

}

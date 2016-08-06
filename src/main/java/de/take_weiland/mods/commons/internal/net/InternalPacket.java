package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.network.NetworkManager;

/**
 * <p>packet that can be sent through NetworkImpl.</p>
 * @author diesieben07
 */
public interface InternalPacket extends SimplePacket {

    String _sc$internal$channel();

    int _sc$internal$expectedSize();

    void _sc$internal$writeTo(MCDataOutput out) throws Exception;

    /**
     * <p>Always called on the network thread.</p>
     * @param side side
     * @param manager NetworkManager
     */
    void _sc$internal$receiveDirect(byte side, NetworkManager manager);

    @Override
    default void sendTo(NetworkManager manager) {
        NetworkImpl.sendPacket(this, manager);
    }
}

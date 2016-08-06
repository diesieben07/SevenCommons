package de.take_weiland.mods.commons.internal.net;

/**
 * @author diesieben07
 */
public interface PacketWithData {

    @Deprecated
    default PacketData _sc$internal$getData() {
        return PacketRegistry.classValue.get(getClass());
    }

}

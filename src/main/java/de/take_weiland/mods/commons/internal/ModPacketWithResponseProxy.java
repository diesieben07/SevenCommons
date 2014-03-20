package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.ModPacket;

import java.util.Map;

/**
 * @author diesieben07
 */
public interface ModPacketWithResponseProxy {

	int _sc$nextTransferId();

	Map<Integer, ModPacket.WithResponse<?>> _sc$transferMap();

}

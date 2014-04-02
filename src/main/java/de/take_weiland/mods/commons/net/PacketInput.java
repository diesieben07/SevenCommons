package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
public interface PacketInput extends DataBuf {

	PacketBuilder.ForResponse response();

	PacketBuilder.ForResponse response(int capacity);

}

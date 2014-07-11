package de.take_weiland.mods.commons.net;

/**
 * A factory used to send packets. Get an instance of this class via one of the methods in the {@link de.take_weiland.mods.commons.net.Network} class
 */
public interface PacketFactory<TYPE extends Enum<TYPE>> {

	/**
	 * create a {@link de.take_weiland.mods.commons.net.PacketBuilder} for the given type
	 *
	 * @param t the Packet type to send
	 * @return a PacketBuilder for the given type
	 */
	PacketBuilder builder(TYPE t);

	/**
	 * like {@link #builder(Enum)} but creates a buffer that can hold at least {@code capacity} bytes before resizing
	 *
	 * @param t        the Packet type to send
	 * @param capacity the initial byte capacity of the buffer
	 * @return a PacketBuilder for the given type
	 */
	PacketBuilder builder(TYPE t, int capacity);

}

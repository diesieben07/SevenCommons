package de.take_weiland.mods.commons.net;

/**
 * <p>A representation of the handler created by {@link Network#newChannel(String)}. Usually you don't need to interact
 * with this interface in any way.</p>
 * @author diesieben07
 */
public interface PacketHandler {

	/**
	 * <p>Create a new MCDataOutputStream that can be used to send packets of the given ID. The ID must be one used when
	 * this PacketHandler was created using {@link de.take_weiland.mods.commons.net.Network#newChannel(String)}.</p>
	 * <p>If you use this method, the {@link de.take_weiland.mods.commons.net.ModPacket#write(MCDataOutputStream)} method
	 * for the corresponding packet class will be bypassed. Only {@link de.take_weiland.mods.commons.net.ModPacket#read(MCDataInputStream, net.minecraft.entity.player.EntityPlayer, cpw.mods.fml.relauncher.Side)}
	 * will be called when the packet is received.</p>
	 * @param packetId the packetId
	 * @return an MCDataOutputStream
	 */
	MCDataOutput createStream(int packetId);

	/**
	 * <p>Same functionality as {@link #createStream(int)}, but creates the stream with the given initial capacity.</p>
	 * @param packetId the packetId
	 * @param initialCapacity the initial capacity for the stream
	 * @return an MCDataOutputStream
	 */
	MCDataOutput createStream(int packetId, int initialCapacity);

	/**
	 * <p>Create a {@code SimplePacket} from an {@code MCDataOutputStream} created by {@code createStream}.</p>
	 * @param stream the MCDataOutputStream
	 * @return a SimplePacket
	 */
	SimplePacket makePacket(MCDataOutput stream);

}

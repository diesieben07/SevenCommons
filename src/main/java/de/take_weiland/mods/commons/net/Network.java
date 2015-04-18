package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.NetworkRegistry;
import de.take_weiland.mods.commons.internal.FMLPacketHandlerImpl;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.SevenCommonsLoader;
import de.take_weiland.mods.commons.netx.*;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * <p>Methods for Network related utilities and setting up your packet handling.</p>
 */
@ParametersAreNonnullByDefault
public final class Network {

	static final Type paramType = de.take_weiland.mods.commons.netx.PacketHandler.class.getTypeParameters()[0];

	/**
	 * gets the INetworkManager associated with the given NetHandler
	 *
	 * @param netHandler the NetHandler to get the INetworkManager from
	 * @return the INetworkManager
	 */
	public static INetworkManager getNetworkManager(NetHandler netHandler) {
		if (!netHandler.isServerHandler()) {
			return SevenCommons.proxy.getNetworkManagerFromClient(netHandler);
		} else if (netHandler instanceof NetServerHandler) {
			return ((NetServerHandler) netHandler).netManager;
		} else {
			return ((NetLoginHandler) netHandler).myTCPConnection;
		}
	}

	/**
	 * <p>Create a new network channel that uses the custom payload packet to send packets.</p>
	 * <p>Each of your packet types has to extend {@link de.take_weiland.mods.commons.net.ModPacket} to use this system.</p>
	 * <p>This method must only be used during mod loading (PreInit, Init, etc. phases).</p>
	 * <p>This method returns a {@code PacketHandlerBuilder} to enable the following fluid interface:
	 * <code><pre>
	 * PacketHandler myChannel = Network.newChannel("example")
	 *                                  .register(PacketA.class)
	 *                                  .register(PacketB.class)
	 *                                  .build();
	 * </pre></code></p>
	 * @param channel the channel to use
	 * @return a new PacketHandlerBuilder for registering your Packet classes
	 */
	public static PacketHandlerBuilder newChannel(String channel) {
		ModContainer mc = Loader.instance().activeModContainer();
		if (mc == null) {
			throw new IllegalStateException("Tried to register a packet channel outside of mod-loading!");
		}
		return new PacketHandlerBuilder(checkChannel(mc.getModId(), channel));
	}

	private static String checkChannel(String activeMod, String channel) {
		checkNotNull(channel, "Channel cannot be null");
		checkArgument(!channel.isEmpty() && channel.length() <= 16, "Invalid channel name");
		checkArgument(!isReserved(channel), "Reserved channel");
		if (isRegistered(channel)) {
			logger.warning(String.format("Channel %s is already registered while registering it for %s!", channel, activeMod));
		}
		return channel;
	}

	private static boolean isRegistered(String channel) {
		NetworkRegistry nr = NetworkRegistry.instance();
		SCReflector r = SCReflector.instance;
		return r.getUniversalPacketHandlers(nr).containsKey(channel)
				|| r.getClientPacketHandlers(nr).containsKey(channel)
				|| r.getServerPacketHandlers(nr).containsKey(channel);
	}

	private static boolean isReserved(String channel) {
		return channel.equals("REGISTER") || channel.equals("UNREGISTER") || channel.startsWith("MC|");
	}

	/**
	 * <p>Create a new MCDataInputStream that reads from the given byte array.</p>
	 * @param buf the array to read from
	 * @return an MCDataInputStream
	 */
	public static MCDataInput newDataInput(byte[] buf) {
		return newDataInput(buf, 0, buf.length);
	}

	/**
	 * <p>Create a new MCDataInputStream that reads at most {@code len} bytes from the given byte array, starting at position
	 * {@code pos}.</p>
	 * @param buf the array to read from
	 * @param off the starting position
	 * @param len the number of bytes to read
	 * @return an MCDataInputStream
	 */
	public static MCDataInput newDataInput(byte[] buf, int off, int len) {
		checkArgument(len >= 0, "len must be >= 0");
		checkArgument(off >= 0, "off must be >= 0");
		checkPositionIndexes(off, off + len, checkNotNull(buf, "buf").length);
		return new MCDataInputImpl(buf, off, len);
	}

	/**
	 * <p>Create a new MCDataOutputStream with a default initial capacity.</p>
	 * @return an MCDataOutputStream
	 */
	public static MCDataOutput newDataOutput() {
		return newDataOutput(FMLPacketHandlerImpl.STREAMS_INITIAL_CAP);
	}

	/**
	 * <p>Create a new MCDataOutputStream with the given initial capacity.</p>
	 * @param initialCapacity the initial capacity for the backing byte array
	 * @return an MCDataOuputStream
	 */
	public static MCDataOutput newDataOutput(int initialCapacity) {
		checkArgument(initialCapacity >= 0, "initialCapacity must be >= 0");
		final int initialCap = initialCapacity;
		return new MCDataOutputImpl(initialCap);
	}

	static final Logger logger = SevenCommonsLoader.scLogger("Packet System");

	private Network() { }
}

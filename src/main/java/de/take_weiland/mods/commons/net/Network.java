package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.transformers.PacketTransformer;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utilities for Network related stuff.
 */
public final class Network {

	private Network() { }

	/**
	 * create a {@link de.take_weiland.mods.commons.net.PacketFactory} that uses the given Channel to send Packets of type {@code TYPE} and dispatches handling off to the given PacketHandler.
	 * @param channel the Packet channel to send on
	 * @param typeClass the Enum class holding the possible Packet types
	 * @param handler the handler to handle the packets
	 * @return a new PacketFactory
	 */
	public static <TYPE extends Enum<TYPE>> PacketFactory<TYPE> makeFactory(String channel, Class<TYPE> typeClass, PacketHandler<TYPE> handler) {
		return new FMLPacketHandlerImpl<>(channel, handler, typeClass);
	}

	/**
	 * <p>create a {@link PacketFactory} that uses the given Channel to send Packets of type {@code TYPE} and dispatches handling off to the corresponding
	 * {@link ModPacket} class of the respective TYPE.</p>
	 * @param channel the Packet channel to send on
	 * @param typeClass the Enum class holding the possible Packet types
	 * @return a new PacketFactory
	 */
	public static <TYPE extends Enum<TYPE> & SimplePacketType> PacketFactory<TYPE> simplePacketHandler(String channel, Class<TYPE> typeClass) {
		PacketFactory<TYPE> factory = makeFactory(channel, typeClass, SimplePacketHandler.<TYPE>instance());
		injectTypesAndFactory(JavaUtils.getEnumConstantsShared(typeClass), factory);
		return factory;
	}

	private static <TYPE extends Enum<TYPE> & SimplePacketType> void injectTypesAndFactory(TYPE[] values, PacketFactory<TYPE> factory) {
		@SuppressWarnings("unchecked")
		SimplePacketTypeProxy proxy = (SimplePacketTypeProxy) values[0];
		checkArgument(proxy._sc$getPacketFactory() == null, "Cannot re-use SimplePacketType classes!");
		proxy._sc$setPacketFactory(factory); // sets a static field, so handles all

		for (TYPE type : values) {
			try {
				Class<?> packetClass = type.packet();
				Field field = packetClass.getDeclaredField(PacketTransformer.TYPE_FIELD);
				field.setAccessible(true);
				field.set(null, type);
			} catch (Exception e) {
				throw new IllegalStateException(String.format("PacketTransformer failed on class %s! SevenCommons was probably installed wrongly!", type.packet().getName()));
			}
		}
	}

	/**
	 * gets the INetworkManager associated with the given NetHandler
	 * @param netHandler the NetHandler to get the INetworkManager from
	 * @return the INetworkManager
	 */
	public static INetworkManager getNetworkManager(NetHandler netHandler) {
		if (!netHandler.isServerHandler()) {
			return SCModContainer.proxy.getNetworkManagerFromClient(netHandler);
		} else if (netHandler instanceof NetServerHandler) {
			return ((NetServerHandler)netHandler).netManager;
		} else {
			return ((NetLoginHandler)netHandler).myTCPConnection;
		}
	}

	static final Logger logger = SevenCommons.scLogger("Packet System");

}

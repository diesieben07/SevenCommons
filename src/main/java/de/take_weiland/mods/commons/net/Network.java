package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.transformers.PacketTransformer;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;

import java.lang.reflect.Field;

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
		return new FMLPacketHandlerImpl<TYPE>(channel, handler, typeClass);
	}

	/**
	 * <p>create a {@link de.take_weiland.mods.commons.net.PacketFactory} that uses the given Channel to send Packets of type {@code TYPE} and dispatches handling off to the corresponding
	 * {@link de.take_weiland.mods.commons.net.ModPacket} class of the respective TYPE.</p>
	 * @param channel the Packet channel to send on
	 * @param typeClass the Enum class holding the possible Packet types
	 * @return a new PacketFactory
	 */
	public static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> PacketFactory<TYPE> simplePacketHandler(String channel, Class<TYPE> typeClass) {
		PacketFactory<TYPE> factory = new FMLPacketHandlerImpl<TYPE>(channel, SimplePacketHandler.<TYPE>instance(), typeClass);
		injectTypesAndFactory(JavaUtils.getEnumConstantsShared(typeClass), factory);
		return factory;
	}

	private static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> void injectTypesAndFactory(TYPE[] values, PacketFactory<TYPE> factory) {
		try {
			for (TYPE e : values) {
				Class<?> packetClass = e.packet();
				Field field = packetClass.getDeclaredField(PacketTransformer.FACTORY_FIELD);
				field.setAccessible(true);
				field.set(null, factory);
				field = packetClass.getDeclaredField(PacketTransformer.TYPE_FIELD);
				field.setAccessible(true);
				field.set(null, e);
			}
		} catch (Exception e) {
			throw new RuntimeException("PacketTransformer failed! SevenCommons was probably installed wrongly!");
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
}

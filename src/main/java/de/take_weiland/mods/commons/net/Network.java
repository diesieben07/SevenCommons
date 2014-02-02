package de.take_weiland.mods.commons.net;

import java.lang.reflect.Field;

import de.take_weiland.mods.commons.util.JavaUtils;

public final class Network {

	private Network() { }
	
	public static <TYPE extends Enum<TYPE>> PacketFactory<TYPE> makeFactory(String channel, Class<TYPE> typeClass, PacketHandler<TYPE> handler) {
		return new FMLPacketHandlerImpl<TYPE>(channel, handler, typeClass);
	}
	
	public static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> PacketFactory<TYPE> simplePacketHandler(String channel, Class<TYPE> typeClass) {
		PacketFactory<TYPE> factory = new FMLPacketHandlerImpl<TYPE>(channel, SimplePacketHandler.<TYPE>instance(), typeClass);
		injectTypesAndFactory(JavaUtils.getEnumValues(typeClass), factory);
		return factory;
	}
	
	private static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> void injectTypesAndFactory(TYPE[] values, PacketFactory<TYPE> factory) {
		try {
			for (TYPE e : values) {
				Class<?> packetClass = e.packet();
				Field field = packetClass.getDeclaredField("_sc_packetfactory");
				field.setAccessible(true);
				field.set(null, factory);
				field = packetClass.getDeclaredField("_sc_packettype");
				field.setAccessible(true);
				field.set(null, e);
			}
		} catch (Exception e) {
			throw new RuntimeException("PacketTransformer failed! SevenCommons was probably installed wrongly!");
		}
	}

}

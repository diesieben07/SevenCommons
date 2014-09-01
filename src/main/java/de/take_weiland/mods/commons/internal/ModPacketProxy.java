package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;

public interface ModPacketProxy {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/ModPacketProxy";
	public static final String GET_HANDLER = "_sc$handler";
	public static final String SET_HANDLER = "_sc$setHandler";
	public static final String CAN_SIDE_RECEIVE = "_sc$canSideReceive";

	void _sc$setHandler(PacketHandlerProxy handler);

	PacketHandlerProxy _sc$handler();

	boolean _sc$canSideReceive(Side side);

}

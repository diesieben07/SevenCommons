package de.take_weiland.mods.commons.netx;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;

class SimplePacketHandler<TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> implements PacketHandler<TYPE> {

	private static final SimplePacketHandler<?> INSTANCE;
	
	static {
		INSTANCE = init();
	}
	
	private static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> SimplePacketHandler<?> init() {
		return new SimplePacketHandler<TYPE>();
	}
	
	@Override
	public void handle(TYPE t, DataBuf buffer, EntityPlayer player, Side side) {
		try {
			t.packet().newInstance().handle(buffer, player, side);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Invalid Packet class, this should not happen!", e);
		}
	}

	@SuppressWarnings("unchecked") // safe, we only call packet()
	public static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> SimplePacketHandler<TYPE> instance() {
		return (SimplePacketHandler<TYPE>) INSTANCE;
	}

}

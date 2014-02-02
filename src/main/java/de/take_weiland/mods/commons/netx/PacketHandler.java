package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

public interface PacketHandler<TYPE extends Enum<TYPE>> {

	void handle(TYPE t, DataBuf buffer, EntityPlayer player, Side side);
	
}

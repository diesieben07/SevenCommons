package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;

public interface PacketHandler<TYPE extends Enum<TYPE>> {

	void handle(TYPE t, DataBuf buffer, EntityPlayer player, Side side);
	
}

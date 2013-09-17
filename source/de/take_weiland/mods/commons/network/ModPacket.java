package de.take_weiland.mods.commons.network;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;

public interface ModPacket extends SendablePacket {

	PacketType type();
	
	byte[] getData(int spareBytes);
	
	void handleData(byte[] data, int offset);
	
	boolean isValidForSide(Side side);

	void execute(EntityPlayer player, Side side);
	
}

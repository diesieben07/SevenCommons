package de.take_weiland.mods.commons.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import cpw.mods.fml.relauncher.Side;

public interface ModPacket extends SendablePacket {

	PacketType type();
	
	byte[] getData(int offset);
	
	void handleData(byte[] data, int offset);
	
	boolean isValidForSide(Side side);

	void execute(EntityPlayer player, Side side);
	
	Packet make();
	
}

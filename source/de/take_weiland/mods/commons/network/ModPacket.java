package de.take_weiland.mods.commons.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;

public interface ModPacket extends SendablePacket {

	PacketType type();
	
	void read(EntityPlayer player, InputStream in) throws IOException;
	
	void write(OutputStream out) throws IOException;
	
	boolean isValidForSide(Side side);
	
	void execute(EntityPlayer player, Side side);
	
	int expectedSize();
	
}

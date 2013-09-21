package de.take_weiland.mods.commons.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.entity.player.EntityPlayer;

public abstract class DataPacket extends AbstractPacket {

	@Override
	public final void read(EntityPlayer player, InputStream in) throws IOException {
		read(player, new DataInputStream(in));
	}
	
	protected abstract void read(EntityPlayer player, DataInputStream in) throws IOException;

	@Override
	public final void write(OutputStream out) throws IOException {
		write(new DataOutputStream(out));
	}
	
	protected abstract void write(DataOutputStream out) throws IOException;

}

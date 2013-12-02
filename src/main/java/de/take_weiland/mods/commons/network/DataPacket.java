package de.take_weiland.mods.commons.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

public abstract class DataPacket extends AbstractPacket {

	@Override
	public final void read(EntityPlayer player, Side side, InputStream in) throws IOException {
		read(player, side, new DataInputStream(in));
	}
	
	protected abstract void read(EntityPlayer player, Side side, DataInputStream in) throws IOException;

	@Override
	public final void write(OutputStream out) throws IOException {
		write(new DataOutputStream(out));
	}
	
	protected abstract void write(DataOutputStream out) throws IOException;

}

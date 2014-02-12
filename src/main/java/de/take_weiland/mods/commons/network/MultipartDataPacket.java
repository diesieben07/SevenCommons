package de.take_weiland.mods.commons.network;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

import java.io.*;

public abstract class MultipartDataPacket extends AbstractMultipartPacket {

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

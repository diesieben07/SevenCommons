package de.take_weiland.mods.commons.network;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public abstract class ModPacket {

	/**
	 * reads this packet's data from the stream
	 * @param in
	 */
	protected abstract void readData(ByteArrayDataInput in);
	
	/**
	 * writes this packet's data to the stream
	 * @param out
	 */
	protected abstract void writeData(ByteArrayDataOutput out);
	
	protected abstract void execute(EntityPlayer player, Side side);
	
	/**
	 * determines if the given side can receive this packet
	 * @param side
	 * @return true if it is valid for the given side to receive this packet
	 */
	protected boolean isValidForSide(Side side) {
		return true;
	}
}

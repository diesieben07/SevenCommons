package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class PacketVersionSelect extends ModPacket {

	private String modId;
	private int index;

	public PacketVersionSelect(String modId, int index) {
		this.modId = modId;
		this.index = index;
	}

	@Override
	protected void write(WritableDataBuf buffer) {
		buffer.putString(modId);
		buffer.putVarInt(index);
	}

	@Override
	protected void handle(DataBuf buffer, EntityPlayer player, Side side) {
		modId = buffer.getString();
		index = buffer.getVarInt();

		SCModContainer.proxy.handleVersionSelect(modId, index);
	}

	@Override
	protected boolean validOn(Side side) {
		return true;
	}
}

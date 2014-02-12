package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import net.minecraft.entity.player.EntityPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static de.take_weiland.mods.commons.net.Packets.readEnum;
import static de.take_weiland.mods.commons.net.Packets.writeEnum;

public class PacketModState extends SCPacket {

	private String modId;
	private ModUpdateState state;
	
	public PacketModState(UpdatableMod mod, ModUpdateState state) {
		this.modId = mod.getModId();
		this.state = state;
	}

	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		modId = in.getString();
		state = readEnum(in, ModUpdateState.class);

		CommonsModContainer.proxy.handleModState(this);
	}

	@Override
	protected void write(WritableDataBuf out) {
		out.putString(modId);
		writeEnum(out, state);
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

	public String getModId() {
		return modId;
	}

	public ModUpdateState getState() {
		return state;
	}
}

package de.take_weiland.mods.commons.internal;

import static de.take_weiland.mods.commons.network.Packets.readEnum;
import static de.take_weiland.mods.commons.network.Packets.writeEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;

public class PacketModState extends DataPacket {

	private String modId;
	private ModUpdateState state;
	
	public PacketModState(UpdatableMod mod, ModUpdateState state) {
		this.modId = mod.getModId();
		this.state = state;
	}

	@Override
	protected void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		modId = in.readUTF();
		state = readEnum(in, ModUpdateState.class);
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		out.writeUTF(modId);
		writeEnum(out, state);
	}

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		CommonsModContainer.proxy.handleModState(this);
	}

	@Override
	public PacketType type() {
		return CommonsPackets.MOD_STATE;
	}

	public String getModId() {
		return modId;
	}

	public ModUpdateState getState() {
		return state;
	}
}

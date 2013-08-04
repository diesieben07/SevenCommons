package de.take_weiland.mods.commons.internal.network;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.network.ModPacket;
import de.take_weiland.mods.commons.network.PacketType;

public class PacketModState extends ModPacket {

	private String modId;
	private ModUpdateState state;
	
	public PacketModState(UpdatableMod mod, ModUpdateState state) {
		this.modId = mod.getModId();
		this.state = state;
	}

	@Override
	protected void readData(ByteArrayDataInput in) {
		modId = in.readUTF();
		state = readEnum(ModUpdateState.class, in);
	}

	@Override
	protected void writeData(ByteArrayDataOutput out) {
		out.writeUTF(modId);
		writeEnum(state, out);
	}

	@Override
	protected boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
		CommonsModContainer.proxy.handleModState(this);
	}

	@Override
	protected PacketType getType() {
		return CommonsPackets.MOD_STATE;
	}

	public String getModId() {
		return modId;
	}

	public ModUpdateState getState() {
		return state;
	}
}

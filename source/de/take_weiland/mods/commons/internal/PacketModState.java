package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.StreamPacket;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public class PacketModState extends StreamPacket {

	private String modId;
	private ModUpdateState state;
	
	public PacketModState(UpdatableMod mod, ModUpdateState state) {
		this.modId = mod.getModId();
		this.state = state;
	}

	@Override
	protected void readData(MinecraftDataInput in) {
		modId = in.readUTF();
		state = in.readEnum(ModUpdateState.class);
	}

	@Override
	protected void writeData(MinecraftDataOutput out) {
		out.writeUTF(modId);
		out.writeEnum(state);
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

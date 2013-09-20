package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.network.AbstractModPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.util.CollectionUtils;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public final class PacketSync extends AbstractModPacket {

	private MinecraftDataOutput out;
	private MinecraftDataInput in;
	
	public PacketSync(MinecraftDataOutput out) {
		this.out = out;
	}

	@Override
	public void handleData(byte[] data, int offset) {
		in = MinecraftDataInput.create(data, offset, data.length);
	}

	@Override
	public byte[] getData(int spareBytes) {
		assert(spareBytes == CommonsModContainer.packetTransport.bytePrefixCount());
		return out.toNewArray();
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		int typeId = in.readUnsignedByte();
		SyncedType<?> type = CollectionUtils.safeArrayAccess(SyncedType.TYPES, typeId);
		if (type != null) {
			SyncedFieldAccessor s = null;
			try {
				s = (SyncedFieldAccessor) type.restoreInstance(player, in);
				if (s != null) {
					Syncing.restoreSyncData(s, in);
				}
			} catch (Exception e) {
				SevenCommons.LOGGER.severe(String.format("Failed to restore SyncData into %s", s));
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public PacketType type() {
		return CommonsPackets.SYNC;
	}

}

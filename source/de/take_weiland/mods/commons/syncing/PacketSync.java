package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.network.AbstractModPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.util.CollectionUtils;

public final class PacketSync extends AbstractModPacket {

	private ByteArrayDataOutput out;
	private ByteArrayDataInput in;
	
	public PacketSync(ByteArrayDataOutput out) {
		this.out = out;
	}

	static void addPacketId(ByteArrayDataOutput out) {
		out.writeByte(CommonsPackets.SYNC.getPacketId());
	}

	@Override
	protected void readData(byte[] data) {
		in = ByteStreams.newDataInput(data, 1);
	}

	@Override
	protected byte[] writeData() {
		return out.toByteArray();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
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
	protected boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	protected PacketType getType() {
		return CommonsPackets.SYNC;
	}

}

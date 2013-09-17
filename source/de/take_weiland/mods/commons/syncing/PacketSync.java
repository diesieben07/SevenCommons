package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

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

	@Override
	public void handleData(byte[] data, int offset) {
		in = ByteStreams.newDataInput(data, offset);
	}

	@Override
	public byte[] getData(int spareBytes) {
		if (spareBytes >= 0) {
			return Bytes.concat(new byte[spareBytes], out.toByteArray());
		} else {
			return out.toByteArray();
		}
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

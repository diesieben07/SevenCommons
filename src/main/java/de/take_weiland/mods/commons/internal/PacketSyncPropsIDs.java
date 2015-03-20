package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public final class PacketSyncPropsIDs extends ModPacket {

	public static void sendToIfNeeded(EntityPlayer player, Entity tracked) {
		List<SyncedEntityProperties> props = ((EntityProxy) tracked)._sc$getSyncedProps();
		if (props != null && !props.isEmpty()) {
			new PacketSyncPropsIDs(tracked).sendTo(player);
		}
	}

	private Entity entity;

	public PacketSyncPropsIDs(Entity entity) {
		this.entity = entity;
	}

	@Override
    public void write(MCDataOutputStream out) {
		out.writeInt(entity.entityId);

		List<SyncedEntityProperties> properties = ((EntityProxy) entity)._sc$getSyncedProps();

		int len = properties.size();
		out.writeVarInt(len);

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < len; i++) {
			out.writeString(properties.get(i)._sc$syncprops$name());
		}
	}

	@Override
    public void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		Entity entity = player.worldObj.getEntityByID(in.readInt());
		if (entity == null) {
			throw new ProtocolException("Received Unknown EntityID!");
		}

		int len = in.readVarInt();
		if (len == 0) {
			((EntityProxy) entity)._sc$setSyncedProps(ImmutableList.<SyncedEntityProperties>of());
		} else {
			SyncedEntityProperties[] arr = new SyncedEntityProperties[len];

			try {
				for (int i = 0; i < len; i++) {
					arr[i] = (SyncedEntityProperties) entity.getExtendedProperties(in.readString());
				}
			} catch (ClassCastException e) {
				throw new ProtocolException("Got ID for non-synced entity properties", e);
			}
			((EntityProxy) entity)._sc$setSyncedProps(Arrays.asList(arr));
		}
	}

	@Override
    public void execute(EntityPlayer player, Side side) throws ProtocolException { }
}

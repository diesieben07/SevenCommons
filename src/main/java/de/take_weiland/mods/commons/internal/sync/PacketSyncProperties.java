package de.take_weiland.mods.commons.internal.sync;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.internal.SyncedEntityProperties;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.ProtocolException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author diesieben07
 */
public class PacketSyncProperties extends ModPacket {

	private Entity entity;

	public PacketSyncProperties(Entity entity) {
		this.entity = entity;
	}

	public static void sendSyncedProperties(EntityPlayer player, Entity entity) {
		if (((EntityProxy) entity)._sc$getSyncedEntityProperties() != null) {
			new PacketSyncProperties(entity).sendTo(player);
		}
	}

	@SuppressWarnings("ForLoopReplaceableByForEach")
	@Override
	protected void write(MCDataOutputStream out) {
		List<SyncedEntityProperties> props = ((EntityProxy) entity)._sc$getSyncedEntityProperties();
		// is not null, checked before sending this packet

		out.writeInt(entity.entityId);

		int len = props.size();
		out.writeVarInt(len);
		// no iterator
		for (int i = 0; i < len; i++) {
			out.writeString(props.get(i)._sc$syncprops$name());
		}
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		int entityId = in.readInt();
		Entity entity = player.worldObj.getEntityByID(entityId);

		int len = in.readVarInt();
		SyncedEntityProperties[] props = new SyncedEntityProperties[len];
		try {
			for (int i = 0; i < len; i++) {
				SyncedEntityProperties prop = (SyncedEntityProperties) entity.getExtendedProperties(in.readString());
				if (prop == null) {
					throw new ProtocolException("Unknown Identifier for IEEP received!");
				}
				props[i] = prop;
			}
		} catch (ClassCastException e) {
			throw new ProtocolException("Identifier for IEEP received, but IEEP is not synced!");
		}
		((EntityProxy) entity)._sc$setSyncedEntityProperties(Arrays.asList(props));
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException { }
}

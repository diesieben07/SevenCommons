package de.take_weiland.mods.commons.internal;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.updater.*;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.List;

public class PacketViewUpdates extends ModPacket {

	private Collection<? extends UpdatableMod> mods;
	private Collection<ClientDummyUpdatableMod> clientMods;
	
	public PacketViewUpdates(UpdateController controller) {
		mods = controller.getMods();
	}

	@Override
	protected void write(WritableDataBuf out) {
		out.putVarInt(mods.size());
		for (UpdatableMod mod : mods) {
			out.putString(mod.getModId());
			out.putString(mod.getName());
			DataBuffers.writeEnum(out, mod.getState());

			mod.getVersions().getCurrentVersion().write(out);

			out.putVarInt(mod.getVersions().getAvailableVersions().size());
			for (ModVersion version : mod.getVersions().getAvailableVersions()) {
				version.write(out);
			}
		}
	}

	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		int modCount = in.getVarInt();
		clientMods = Lists.newArrayListWithCapacity(modCount);
		
		for (int i = 0; i < modCount; i++) {
			ClientDummyUpdatableMod mod = new ClientDummyUpdatableMod();
			String modId = in.getString();
			String name = in.getString();
			
			ModUpdateState state = DataBuffers.readEnum(in, ModUpdateState.class);
			
			ModVersion current = ModVersion.read(mod, in);
			int versionCount = in.getVarInt();
			List<ModVersion> versions = Lists.newArrayListWithCapacity(versionCount);
			for (int j = 0; j < versionCount; j++) {
				versions.add(ModVersion.read(mod, in));
			}
			
			mod.setModId(modId);
			mod.setName(name);
			mod.setState(state);
			
			ModVersionCollection versionCollection = new ModVersionCollection(mod, current);
			mod.setVersions(versionCollection);
			
			versionCollection.injectAvailableVersions(versions);
			clientMods.add(mod);
		}

		SCModContainer.proxy.handleViewUpdates(this);
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}
	
	public Collection<ClientDummyUpdatableMod> getMods() {
		return clientMods;
	}

}

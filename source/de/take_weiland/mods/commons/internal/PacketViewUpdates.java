package de.take_weiland.mods.commons.internal;

import java.util.Collection;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.ClientDummyUpdatableMod;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.ModVersion;
import de.take_weiland.mods.commons.internal.updater.ModVersionCollection;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.StreamPacket;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public class PacketViewUpdates extends StreamPacket {

	private Collection<? extends UpdatableMod> mods;
	private Collection<ClientDummyUpdatableMod> clientMods;
	
	public PacketViewUpdates(UpdateController controller) {
		mods = controller.getMods();
	}
	
	@Override
	protected void readData(MinecraftDataInput in) {
		int modCount = in.readUnsignedShort();
		clientMods = Lists.newArrayListWithCapacity(modCount);
		
		for (int i = 0; i < modCount; i++) {
			ClientDummyUpdatableMod mod = new ClientDummyUpdatableMod();
			String modId = in.readUTF();
			String name = in.readUTF();
			
			ModUpdateState state = in.readEnum(ModUpdateState.class);
			
			ModVersion current = ModVersion.read(mod, in);
			int versionCount = in.readUnsignedShort();
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
	}

	@Override
	protected void writeData(MinecraftDataOutput out) {
		out.writeShort(mods.size());
		for (UpdatableMod mod : mods) {
			out.writeUTF(mod.getModId());
			out.writeUTF(mod.getName());
			out.writeEnum(mod.getState());
			
			mod.getVersions().getCurrentVersion().write(out);
			
			out.writeShort(mod.getVersions().getAvailableVersions().size());
			for (ModVersion version : mod.getVersions().getAvailableVersions()) {
				version.write(out);
			}
		}
	}

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}
	
	@Override
	public void execute(EntityPlayer player, Side side) {
		CommonsModContainer.proxy.handleViewUpdates(this);
	}

	@Override
	public PacketType type() {
		return CommonsPackets.VIEW_UPDATES;
	}

	public Collection<ClientDummyUpdatableMod> getMods() {
		return clientMods;
	}

}

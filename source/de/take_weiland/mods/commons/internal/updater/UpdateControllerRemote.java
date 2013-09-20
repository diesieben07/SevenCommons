package de.take_weiland.mods.commons.internal.updater;

import static cpw.mods.fml.common.network.PacketDispatcher.sendPacketToServer;

import java.util.Collection;

import com.google.common.collect.Maps;

import de.take_weiland.mods.commons.internal.PacketUpdateAction;
import de.take_weiland.mods.commons.internal.PacketUpdateAction.Action;

public class UpdateControllerRemote extends AbstractUpdateController {

	public UpdateControllerRemote(Collection<ClientDummyUpdatableMod> mods) {
		this.mods = Maps.uniqueIndex(mods, ID_RETRIEVER);
		for (ClientDummyUpdatableMod mod : mods) {
			mod.setController(this);
		}
	}
	
	@Override
	public void searchForUpdates() {
		sendPacketToServer(new PacketUpdateAction(Action.SEARCH_ALL).make());
	}

	@Override
	public void searchForUpdates(UpdatableMod mod) {
		sendPacketToServer(new PacketUpdateAction(Action.SEARCH, mod.getModId()).make());
	}

	@Override
	public void update(UpdatableMod mod, ModVersion version) {
		sendPacketToServer(new PacketUpdateAction(Action.UPDATE, mod.getModId(), mod.getVersions().getAvailableVersions().indexOf(version)).make());
	}

	@Override
	public boolean restartMinecraft() {
		sendPacketToServer(new PacketUpdateAction(Action.RESTART_MINECRAFT).make());
		return true;
	}

}

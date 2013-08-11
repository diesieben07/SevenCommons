package de.take_weiland.mods.commons.internal.updater;

import java.util.Collection;

import com.google.common.collect.Maps;

import de.take_weiland.mods.commons.internal.PacketUpdateAction;
import de.take_weiland.mods.commons.internal.PacketUpdateAction.Action;

public class UpdateControllerRemote extends AbstractUpdateController {

	public UpdateControllerRemote(Collection<UpdatableMod> mods) {
		this.mods = Maps.uniqueIndex(mods, ID_RETRIEVER);
	}
	
	@Override
	public void searchForUpdates() {
		new PacketUpdateAction(Action.SEARCH_ALL).sendToServer();
	}

	@Override
	public void searchForUpdates(UpdatableMod mod) {
		new PacketUpdateAction(Action.SEARCH, mod.getModId()).sendToServer();
	}

	@Override
	public void update(UpdatableMod mod, ModVersion version) {
		new PacketUpdateAction(Action.UPDATE, mod.getModId(), mod.getVersions().getAvailableVersions().indexOf(version)).sendToServer();
	}

}

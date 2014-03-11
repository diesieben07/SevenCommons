package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.updater.ModVersion;
import de.take_weiland.mods.commons.internal.updater.PlayerUpdateInformation;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import static de.take_weiland.mods.commons.net.DataBuffers.readEnum;
import static de.take_weiland.mods.commons.net.DataBuffers.writeEnum;

public class PacketUpdateAction extends ModPacket {

	private Action action;
	private String modId;
	private int versionIndex = -1;
	
	public PacketUpdateAction(Action action) {
		if (action.hasModId || action.hasVersionIndex) {
			wrongArgs(action);
		}
		this.action = action;
	}
	
	public PacketUpdateAction(Action action, String modId) {
		if (!action.hasModId || action.hasVersionIndex) {
			wrongArgs(action);
		}
		this.action = action;
		this.modId = modId;
	}
	
	public PacketUpdateAction(Action action, String modId, int versionIndex) {
		if (!action.hasModId || !action.hasVersionIndex) {
			wrongArgs(action);
		}
		this.action = action;
		this.modId = modId;
		this.versionIndex = versionIndex;
	}
	
	private static void wrongArgs(Action action) {
		throw new IllegalArgumentException("Wrong argument count for action " + action);
	}

	@Override
	protected void write(WritableDataBuf out) {
		writeEnum(out, action);
		if (action.hasModId) {
			out.putString(modId);
		}
		if (action.hasVersionIndex) {
			out.putUnsignedShort(versionIndex);
		}
	}
	
	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		action = readEnum(in, Action.class);
		if (action.hasModId) {
			modId = in.getString();
		}
		if (action.hasVersionIndex) {
			versionIndex = in.getUnsignedShort();
		}

		if (!player.canCommandSenderUseCommand(4, SCModContainer.updateCommand)) {
			player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("sevencommons.updates.noop").setColor(EnumChatFormatting.RED));
		} else if (!SCModContainer.updaterEnabled) {
			player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("sevencommons.updates.disabled"));
		} else {
			UpdateController localUpdater = SCModContainer.updateController;
			UpdatableMod mod = modId == null ? null : localUpdater.getMod(modId);
			switch (action) {
				case SEARCH:
					localUpdater.searchForUpdates(mod);
					break;
				case SEARCH_ALL:
					localUpdater.searchForUpdates();
					break;
				case UPDATE:
					ModVersion version = JavaUtils.get(mod.getVersions().getAvailableVersions(), versionIndex);
					if (version != null) {
						localUpdater.update(mod, version);
					}
					break;
				case CLOSE_SCREEN:
					localUpdater.unregisterListener((PlayerUpdateInformation)player.getExtendedProperties(PlayerUpdateInformation.IDENTIFIER));
					break;
				case RESTART_MINECRAFT:
					if (!localUpdater.restartMinecraft()) {
						new PacketClientAction(PacketClientAction.Action.RESTART_FAILURE).sendTo(player);
					}
			}
		}
	}

	@Override
	public boolean validOn(Side side) {
		return side.isServer();
	}

	public static enum Action {
		
		SEARCH_ALL(false, false), SEARCH(true, false), UPDATE(true, true), CLOSE_SCREEN(false, false), RESTART_MINECRAFT(false, false);
		
		final boolean hasModId;
		final boolean hasVersionIndex;
		
		private Action(boolean hasModId, boolean hasVersionIndex) {
			this.hasModId = hasModId;
			this.hasVersionIndex = hasVersionIndex;
		}
		
	}
}

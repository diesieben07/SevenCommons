package de.take_weiland.mods.commons.internal;

import static de.take_weiland.mods.commons.network.Packets.readEnum;
import static de.take_weiland.mods.commons.network.Packets.writeEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.ModVersion;
import de.take_weiland.mods.commons.internal.updater.PlayerUpdateInformation;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.util.CollectionUtils;

public class PacketUpdateAction extends DataPacket {

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
	protected void read(EntityPlayer player, DataInputStream in) throws IOException {
		action = readEnum(in, Action.class);
		if (action.hasModId) {
			modId = in.readUTF();
		}
		if (action.hasVersionIndex) {
			versionIndex = in.readUnsignedShort();
		}
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		writeEnum(out, action);
		if (action.hasModId) {
			out.writeUTF(modId);
		}
		if (action.hasVersionIndex) {
			out.writeShort(versionIndex);
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		if (!player.canCommandSenderUseCommand(4, CommonsModContainer.updateCommand)) {
			player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("sevencommons.updates.noop").setColor(EnumChatFormatting.RED));
		} else if (!CommonsModContainer.updaterEnabled) {
			player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("sevencommons.updates.disabled"));
		} else {
			UpdateController localUpdater = CommonsModContainer.updateController;
			UpdatableMod mod = modId == null ? null : localUpdater.getMod(modId);
			switch (action) {
			case SEARCH:
				localUpdater.searchForUpdates(mod);
				break;
			case SEARCH_ALL:
				localUpdater.searchForUpdates();
				break;
			case UPDATE:
				ModVersion version = CollectionUtils.safeListAccess(mod.getVersions().getAvailableVersions(), versionIndex);
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
	public boolean isValidForSide(Side side) {
		return side.isServer();
	}

	@Override
	public PacketType type() {
		return CommonsPackets.UPDATE_ACTION;
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

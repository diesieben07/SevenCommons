package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.ModVersion;
import de.take_weiland.mods.commons.internal.updater.PlayerUpdateInformation;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.network.StreamPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.util.CollectionUtils;

public class PacketUpdateAction extends StreamPacket {

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
	protected void readData(ByteArrayDataInput in) {
		action = readEnum(Action.class, in);
		if (action.hasModId) {
			modId = in.readUTF();
		}
		if (action.hasVersionIndex) {
			versionIndex = in.readUnsignedShort();
		}
	}

	@Override
	protected void writeData(ByteArrayDataOutput out) {
		writeEnum(action, out);
		if (action.hasModId) {
			out.writeUTF(modId);
		}
		if (action.hasVersionIndex) {
			out.writeShort(versionIndex);
		}
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
		if (!player.canCommandSenderUseCommand(4, CommonsModContainer.updateCommand)) {
			player.sendChatToPlayer(ChatMessageComponent.func_111077_e("sevencommons.updates.noop").func_111059_a(EnumChatFormatting.RED));
		} else if (!CommonsModContainer.updaterEnabled) {
			player.sendChatToPlayer(ChatMessageComponent.func_111077_e("sevencommons.updates.disabled"));
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
	protected boolean isValidForSide(Side side) {
		return side.isServer();
	}

	@Override
	protected PacketType getType() {
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

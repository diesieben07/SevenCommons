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
		this.action = action;
	}
	
	public PacketUpdateAction(Action action, String modIndex) {
		this.action = action;
		this.modId = modIndex;
	}
	
	public PacketUpdateAction(Action action, String modIndex, int versionIndex) {
		this.action = action;
		this.modId = modIndex;
		this.versionIndex = versionIndex;
	}
	
	@Override
	protected void readData(ByteArrayDataInput in) {
		action = readEnum(Action.class, in);
		if (action != Action.SEARCH_ALL && action != Action.CLOSE_SCREEN) {
			modId = in.readUTF();
		}
		if (action == Action.UPDATE) {
			versionIndex = in.readUnsignedShort();
		}
	}

	@Override
	protected void writeData(ByteArrayDataOutput out) {
		writeEnum(action, out);
		if (modId != null) {
			out.writeUTF(modId);
		}
		if (versionIndex > 0) {
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
		
		SEARCH_ALL, SEARCH, UPDATE, CLOSE_SCREEN
		
	}
}

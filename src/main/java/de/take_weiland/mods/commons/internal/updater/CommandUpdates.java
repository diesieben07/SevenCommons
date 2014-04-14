package de.take_weiland.mods.commons.internal.updater;

import de.take_weiland.mods.commons.internal.PacketDisplayUpdates;
import de.take_weiland.mods.commons.internal.ServerProxy;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandUpdates extends CommandBase {

	private final String command;
	
	public CommandUpdates(String command) {
		this.command = command;
	}
	
	@Override
	public String getCommandName() {
		return command;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + command;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("sevencommons.updates.noplayer");
		} else if (!SCModContainer.updaterEnabled) {
			throw new CommandException("sevencommons.updates.disabled");
		} else if (ServerProxy.currentUpdateViewer == null) {
			EntityPlayer player = (EntityPlayer) sender;
			ServerProxy.currentUpdateViewer = player;
			new PacketDisplayUpdates().sendTo(player);
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

    @Override
    public int compareTo(Object iCommand) {
	   return getCommandName().compareTo(((ICommand) iCommand).getCommandName());
    }

}

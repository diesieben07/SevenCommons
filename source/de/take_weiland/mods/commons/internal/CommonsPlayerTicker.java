package de.take_weiland.mods.commons.internal;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import de.take_weiland.mods.commons.util.ModdingUtils;

public class CommonsPlayerTicker implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		ModdingUtils.setActivePlayer((EntityPlayer) tickData[0]);
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		ModdingUtils.setActivePlayer(null);
	}

	private static final EnumSet<TickType> ticks = EnumSet.of(TickType.PLAYER);
	
	@Override
	public EnumSet<TickType> ticks() {
		return ticks;
	}

	@Override
	public String getLabel() {
		return "SevenCommonsPlayerTick";
	}

}

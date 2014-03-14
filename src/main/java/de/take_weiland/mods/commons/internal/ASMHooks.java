package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.ZombieConvertEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.metadata.HasMetadata;
import de.take_weiland.mods.commons.metadata.Metadata;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.MiscUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

/**
 * A class containing methods called from ASM generated code.
 * @author diesieben07
 *
 */
@SuppressWarnings("unused") // called from ASM generated code only
public final class ASMHooks {

	private ASMHooks() { }

	private static final int ZOMBIE_IS_CONVERTING_FLAG = 14;
	
	public static void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}
	
	public static EntityVillager onZombieConvert(EntityZombie zombie, EntityVillager villager) {
		ZombieConvertEvent event = new ZombieConvertEvent(zombie, villager);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			zombie.getDataWatcher().updateObject(ZOMBIE_IS_CONVERTING_FLAG, Byte.valueOf((byte)0)); // reset the isConverting flag if the event was canceled
			return null;
		} else {
			return event.villager;
		}
	}

	@SideOnly(Side.CLIENT)
	public static void onGuiInit(GuiScreen gui) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, MiscUtil.getReflector().getButtonList(gui)));
	}
	
	public static void onStartTracking(EntityPlayer player, Entity tracked) {
		SyncASMHooks.syncEntityPropertyIds(player, tracked);
		MinecraftForge.EVENT_BUS.post(new PlayerStartTrackingEvent(player, tracked));
	}

	public static <T extends Enum<T> & Metadata.Simple> Metadata getSimpleMetadata(HasMetadata.Simple<T> holder, World world, int x, int y, int z) {
		return JavaUtils.byOrdinal(holder.metaClass(), world.getBlockMetadata(x, y, z));
	}

	public static <T extends Enum<T> & Metadata.Simple> Metadata getSimpleMetadata(HasMetadata.Simple<T> holder, ItemStack stack) {
		return JavaUtils.byOrdinal(holder.metaClass(), MiscUtil.getReflector().getRawDamage(stack));
	}

	public static <T extends Enum<T> & Metadata.Simple> void injectMetadataHolder(HasMetadata.Simple<T> holder) {
		SimpleMetadataProxy proxy = (SimpleMetadataProxy) JavaUtils.byOrdinal(holder.metaClass(), 0);
		if (proxy._sc$getMetadataHolder() != null) {
			throw new IllegalStateException(String.format("Cannot reuse Metadata.Simple class %s!", holder.metaClass().getName()));
		}
		((SimpleMetadataProxy) JavaUtils.byOrdinal(holder.metaClass(), 0))._sc$injectMetadataHolder(holder);
	}

}

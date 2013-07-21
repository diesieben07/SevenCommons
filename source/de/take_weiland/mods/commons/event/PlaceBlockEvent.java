package de.take_weiland.mods.commons.event;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.player.PlayerEvent;

public abstract class PlaceBlockEvent extends PlayerEvent {

	public final World world;
	public final int x;
	public final int y;
	public final int z;
	public final ItemStack item;
	
	public PlaceBlockEvent(EntityPlayer player, int x, int y, int z, ItemStack item) {
		super(player);
		this.world = player.worldObj;
		this.x = x;
		this.y = y;
		this.z = z;
		this.item = item;
	}

	/**
	 * Fired before the block is set into the world
	 * @author diesieben07
	 *
	 */
	public static class Pre extends PlaceBlockEvent {

		public Pre(EntityPlayer player, int x, int y, int z, ItemStack item) {
			super(player, x, y, z, item);
		}
		
	}
	
	/**
	 * Fired after the block is set into the world and all post processing is done
	 * ({@link Block#onBlockPlacedBy}, {@link Block#onPostBlockPlaced}
	 * @author diesieben07
	 *
	 */
	@Cancelable
	public static class Post extends PlaceBlockEvent {

		public Post(EntityPlayer player, int x, int y, int z, ItemStack item) {
			super(player, x, y, z, item);
		}
		
	}
}

package de.take_weiland.mods.commons.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class BlockChangeEvent extends LivingEvent {

	public final World world;
	public final int x;
	public final int y;
	public final int z;
	public final int newBlockId;
	public final int newMeta;
	
	public BlockChangeEvent(EntityLivingBase entity, World world, int x, int y, int z, int newBlockId, int newMeta) {
		super(entity);
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.newBlockId = newBlockId;
		this.newMeta = newMeta;
	}
	
	@Cancelable
	public static class Pre extends BlockChangeEvent {

		public Pre(EntityLivingBase entity, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(entity, world, x, y, z, newBlockId, newMeta);
		}
		
	}
	
	public static class Post extends BlockChangeEvent {

		public Post(EntityLivingBase entity, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(entity, world, x, y, z, newBlockId, newMeta);
		}
		
	}
	
//	public static 
	
}

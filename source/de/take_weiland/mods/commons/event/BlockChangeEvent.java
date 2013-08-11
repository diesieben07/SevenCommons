package de.take_weiland.mods.commons.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;

public abstract class BlockChangeEvent extends Event {

	private TileEntity teCause;
	private EntityLivingBase entityCause;
	private EntityPlayer playerCause;
	
	public final World world;
	public final int x;
	public final int y;
	public final int z;
	public final int newBlockId;
	public final int newMeta;
	
	public BlockChangeEvent(EntityPlayer player, World world, int x, int y, int z, int newBlockId, int newMeta) {
		this(world, x, y, z, newBlockId, newMeta);
		
		this.entityCause = this.playerCause = player;
	}
	
	public BlockChangeEvent(EntityLivingBase entity, World world, int x, int y, int z, int newBlockId, int newMeta) {
		this(world, x, y, z, newBlockId, newMeta);
	
		this.entityCause = entity;
		if (entity instanceof EntityPlayer) {
			this.playerCause = (EntityPlayer)entity;
		}
	}
	
	public BlockChangeEvent(TileEntity te, World world, int x, int y, int z, int newBlockId, int newMeta) {
		this(world, x, y, z, newBlockId, newMeta);
		
		this.teCause = te;
	}
	
	public BlockChangeEvent(World world, int x, int y, int z, int newBlockId, int newMeta) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.newBlockId = newBlockId;
		this.newMeta = newMeta;
	}
	
	public EntityLivingBase getEntity() {
		return entityCause;
	}
	
	public TileEntity getTileEntity() {
		return teCause;
	}
	
	public EntityPlayer getPlayer() {
		return playerCause;
	}
	
	public boolean isEntityCaused() {
		return entityCause != null;
	}
	
	public boolean isPlayerCaused() {
		return playerCause != null;
	}
	
	public boolean isTileEntityCaused() {
		return teCause != null;
	}
	
	@Cancelable
	public static class Pre extends BlockChangeEvent {

		public Pre(EntityLivingBase entity, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(entity, world, x, y, z, newBlockId, newMeta);
		}

		public Pre(EntityPlayer player, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(player, world, x, y, z, newBlockId, newMeta);
		}

		public Pre(TileEntity te, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(te, world, x, y, z, newBlockId, newMeta);
		}

		public Pre(World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(world, x, y, z, newBlockId, newMeta);
		}
		
	}
	
	public static class Post extends BlockChangeEvent {

		public Post(EntityLivingBase entity, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(entity, world, x, y, z, newBlockId, newMeta);
		}

		public Post(EntityPlayer player, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(player, world, x, y, z, newBlockId, newMeta);
		}

		public Post(TileEntity te, World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(te, world, x, y, z, newBlockId, newMeta);
		}

		public Post(World world, int x, int y, int z, int newBlockId, int newMeta) {
			super(world, x, y, z, newBlockId, newMeta);
		}

	}
	
}

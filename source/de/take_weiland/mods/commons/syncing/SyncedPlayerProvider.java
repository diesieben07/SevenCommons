package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import de.take_weiland.mods.commons.util.Containers;
import de.take_weiland.mods.commons.util.Entities;

public interface SyncedPlayerProvider<T> {

	Iterable<? extends EntityPlayer> getAssociatedPlayers(T object);
	
	public static final SyncedPlayerProvider<Entity> FOR_ENTITY = new SyncedPlayerProvider<Entity>() {

		@Override
		public Iterable<? extends EntityPlayer> getAssociatedPlayers(Entity entity) {
			return Entities.getTrackingPlayers(entity);
		}
		
	};
	
	public static final SyncedPlayerProvider<Container> FOR_CONTAINER = new SyncedPlayerProvider<Container>() {

		@Override
		public Iterable<EntityPlayer> getAssociatedPlayers(Container container) {
			return Containers.getViewingPlayers(container);
		}
	};

}

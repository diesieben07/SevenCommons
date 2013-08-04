package de.take_weiland.mods.commons.event;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Fired when a ZombieVillager gets converted to a Villager
 * @author diesieben07
 *
 */
@Cancelable
public class ZombieConvertEvent extends LivingEvent {

	/**
	 * the zombie being converted
	 */
	public final EntityZombie zombie;
	
	/**
	 * the villager being spawned by this zombie<br>
	 * you may change or modify this
	 */
	public EntityVillager villager;
	
	public ZombieConvertEvent(EntityZombie entity, EntityVillager villager) {
		super(entity);
		zombie = entity;
		this.villager = villager;
	}

}

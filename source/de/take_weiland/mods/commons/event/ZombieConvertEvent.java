package de.take_weiland.mods.commons.event;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Fired when a ZombieVillager gets converted to a Villager
 * @author diesieben07
 *
 */
@Cancelable
public class ZombieConvertEvent extends LivingEvent {

	public ZombieConvertEvent(EntityZombie entity) {
		super(entity);
	}

}

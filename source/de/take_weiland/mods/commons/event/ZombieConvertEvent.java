package de.take_weiland.mods.commons.event;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

@Cancelable
public class ZombieConvertEvent extends LivingEvent {

	public ZombieConvertEvent(EntityZombie entity) {
		super(entity);
	}

}

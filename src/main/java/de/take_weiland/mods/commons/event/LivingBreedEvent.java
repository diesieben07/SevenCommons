package de.take_weiland.mods.commons.event;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * fired when the breeding AI spawns the baby and experience
 * @author diesieben07
 *
 */
@Cancelable
public final class LivingBreedEvent extends LivingEvent {

	/**
	 * the animal spawning the child
	 */
	public final EntityAnimal animal;
	
	/**
	 * the animal's mate
	 */
	public final EntityAnimal mate;
	
	/**
	 * the child being spawned
	 */
	public final EntityAgeable child;
	
	/**
	 * the amount of experience being spawned, may be changed
	 */
	public int xp;
	
	public LivingBreedEvent(EntityAnimal animal, EntityAnimal mate, EntityAgeable child) {
		super(animal);
		this.animal = animal;
		this.mate = mate;
		this.child = child;
	}
}

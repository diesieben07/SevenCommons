package de.take_weiland.mods.commons.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import com.google.common.base.Function;

/**
 * Apply this annotation to a field to automatically synchronize it between Server & Client<br>
 * This only works for fields in Entities, TileEntities, Containers and instances of IExtendedEntityProperties<br>
 * You can use {@link Syncing#registerSyncer} to register your own generic TypeSyncer.<br>
 * If you want to override the default behavior for a specific field use the <code>useSyncer</code> property in conjuction with @DefineSyncer<br>
 * Default supported types are all primitives (Syncing method can not be changed!), {@link ItemStack ItemStacks}, {@link FluidStack FluidStacks}, {@link String Strings}, {@link Enum Enums} and {@link FluidTank FluidTanks} (not {@link IFluidTank IFluidTank}!)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Synced {

	/**
	 * define the {@link TypeSyncer} to use with for this field<br>
	 * Only supported for non-primitive fields.<br>
	 * Use @DefineSyncer to define a syncer
	 * @return
	 */
	int useSyncer() default -1;
	
	/**
	 * Assign this field to a sync group. Fields in the same sync group will be synced in one operation.<br>
	 * The default value (-1) uses the default behaviour (see {@link Synced @Synced})
	 * You can get a Packet for a specific group via {@link SyncGroupHandler @SyncGroupHandler}.
	 * @return
	 */
	int syncGroup() default -1;
	
	/**
	 * Apply this to a field of type {@link TypeSyncer} to define a syncer to use for a @Synced field in this class
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	static @interface DefineSyncer {
		
		int value();
		
	}
	
	/**
	 * Apply this to a static field of type <code>{@link Function Function}&lt;T, {@link Packet}&gt;</code>, where T is the type of this class<br>
	 * You can use the function being injected into this field for obtaining an optional packet required to synchronize the given syncGroup
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	static @interface SyncGroupHandler {
		
		/**
		 * The syncGroup id to get a Packet for
		 * @return
		 */
		int syncGroup();
		
	}
}
